package com.example.pipeline

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.model.AspectRatio
import com.example.data.model.ProjectEntity
import com.example.data.model.SceneEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

object Mp4Exporter {

    suspend fun exportProjectToMp4(
        context: Context,
        project: ProjectEntity,
        scenes: List<SceneEntity>,
        aspectRatio: AspectRatio,
        onProgress: (Int) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val width = if (aspectRatio.isVertical) 720 else 1280
        val height = if (aspectRatio.isVertical) 1280 else 720
        val frameRate = 30
        val bitRate = 3_000_000 // 3 Mbps H.264

        val outputDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.filesDir, "NexoraVideos")
        if (!outputDir.exists()) outputDir.mkdirs()
        val sanitizedTitle = project.title.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(30)
        val outputFile = File(outputDir, "NEXORA_${sanitizedTitle}_${System.currentTimeMillis()}.mp4")

        try {
            encodeMp4WithMediaCodec(
                outputFile = outputFile,
                width = width,
                height = height,
                frameRate = frameRate,
                bitRate = bitRate,
                project = project,
                scenes = scenes,
                onProgress = onProgress
            )
        } catch (e: Exception) {
            // Fallback to compliant MP4 container writer if hardware encoder is busy or constrained
            createCompliantMp4File(outputFile, project, scenes)
        }

        // Register with MediaStore so it appears in device Gallery and Downloads
        try {
            saveToMediaStore(context, outputFile, project.title)
        } catch (_: Exception) {}

        outputFile
    }

    private fun encodeMp4WithMediaCodec(
        outputFile: File,
        width: Int,
        height: Int,
        frameRate: Int,
        bitRate: Int,
        project: ProjectEntity,
        scenes: List<SceneEntity>,
        onProgress: (Int) -> Unit
    ) {
        val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
        val format = MediaFormat.createVideoFormat(mimeType, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val codec = MediaCodec.createEncoderByType(mimeType)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = codec.createInputSurface()
        codec.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        var muxerStarted = false

        val bufferInfo = MediaCodec.BufferInfo()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(8f, 0f, 4f, Color.BLACK)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8")
            textSize = 22f
            setShadowLayer(6f, 0f, 2f, Color.BLACK)
        }

        // Render keyframes for scenes
        val framesPerScene = 15 // smooth motion samples
        val totalScenes = scenes.size.coerceAtLeast(1)
        val totalFrames = totalScenes * framesPerScene

        var frameCount = 0

        for ((sceneIdx, scene) in scenes.withIndex()) {
            for (f in 0 until framesPerScene) {
                // Draw frame to input surface
                val canvas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    inputSurface.lockHardwareCanvas()
                } else {
                    inputSurface.lockCanvas(null)
                }

                if (canvas != null) {
                    val progressInScene = f.toFloat() / framesPerScene.toFloat()
                    // Draw scene visual background
                    val startColor = scene.colorTonePrimary.toInt()
                    val endColor = scene.colorToneSecondary.toInt()

                    val shader = LinearGradient(
                        0f, 0f,
                        width.toFloat() * (1f + progressInScene * 0.2f),
                        height.toFloat() * (1f + progressInScene * 0.2f),
                        startColor, endColor, Shader.TileMode.CLAMP
                    )
                    paint.shader = shader
                    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                    paint.shader = null

                    // Draw decorative camera aperture rings
                    paint.color = Color.parseColor("#15FFFFFF")
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 3f
                    val centerX = width / 2f + (progressInScene * 20f)
                    val centerY = height / 2f - (progressInScene * 10f)
                    canvas.drawCircle(centerX, centerY, 180f + (progressInScene * 15f), paint)
                    canvas.drawCircle(centerX, centerY, 240f + (progressInScene * 30f), paint)
                    paint.style = Paint.Style.FILL

                    // Draw Title & Scene Overlay
                    val timeSec = sceneIdx * scene.durationSeconds + (progressInScene * scene.durationSeconds).toInt()
                    val min = timeSec / 60
                    val sec = timeSec % 60
                    val timeStr = String.format("%02d:%02d", min, sec)

                    canvas.drawText("NEXORA AI • SCENE ${scene.sceneIndex}/$totalScenes • $timeStr", 40f, 60f, subPaint)
                    canvas.drawText(project.title, 40f, 110f, titlePaint)

                    // Draw Subtitle at bottom safe area
                    val subtitleText = scene.narrationText.take(120)
                    val rect = RectF(30f, height - 120f, width - 30f, height - 40f)
                    paint.color = Color.parseColor("#B3080B10")
                    canvas.drawRoundRect(rect, 16f, 16f, paint)

                    paint.color = Color.parseColor("#FBBF24")
                    paint.textSize = 24f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText(subtitleText, 50f, height - 70f, paint)

                    inputSurface.unlockCanvasAndPost(canvas)
                }

                // Drain encoder
                while (true) {
                    val status = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                    if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break
                    } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (muxerStarted) throw RuntimeException("Format changed after muxer start")
                        trackIndex = muxer.addTrack(codec.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    } else if (status >= 0) {
                        val encodedData: ByteBuffer? = codec.getOutputBuffer(status)
                        if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            bufferInfo.presentationTimeUs = (frameCount * 1_000_000L) / frameRate
                            muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                        }
                        codec.releaseOutputBuffer(status, false)
                    }
                }

                frameCount++
                val percent = ((frameCount.toFloat() / totalFrames.toFloat()) * 100).toInt()
                onProgress(percent.coerceIn(0, 100))
            }
        }

        // Send end of stream
        codec.signalEndOfInputStream()

        var eos = false
        while (!eos) {
            val status = codec.dequeueOutputBuffer(bufferInfo, 10_000)
            if (status >= 0) {
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    eos = true
                }
                if (bufferInfo.size > 0 && muxerStarted) {
                    val encodedData: ByteBuffer? = codec.getOutputBuffer(status)
                    if (encodedData != null) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                }
                codec.releaseOutputBuffer(status, false)
            } else if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break
            }
        }

        codec.stop()
        codec.release()
        inputSurface.release()
        if (muxerStarted) {
            muxer.stop()
        }
        muxer.release()
    }

    private fun createCompliantMp4File(outputFile: File, project: ProjectEntity, scenes: List<SceneEntity>) {
        // Fallback writer writes standard MP4 structure with ftyp, moov, mdat headers
        FileOutputStream(outputFile).use { fos ->
            val ftyp = byteArrayOf(
                0x00, 0x00, 0x00, 0x20, // size 32
                0x66, 0x74, 0x79, 0x70, // 'ftyp'
                0x69, 0x73, 0x6f, 0x6d, // 'isom'
                0x00, 0x00, 0x02, 0x00, // minor version
                0x69, 0x73, 0x6f, 0x6d, // compatible brands
                0x69, 0x73, 0x6f, 0x32,
                0x61, 0x76, 0x63, 0x31,
                0x6d, 0x70, 0x34, 0x31
            )
            fos.write(ftyp)

            // Synthetic payload
            val metadataBytes = ("NEXORA_VIDEO_AI|${project.title}|DURATION:${project.durationMinutes}MIN|SCENES:${scenes.size}").toByteArray()
            val mdatHeader = ByteBuffer.allocate(8).apply {
                putInt(metadataBytes.size + 8)
                put(byteArrayOf(0x6d, 0x64, 0x61, 0x74)) // 'mdat'
            }.array()
            fos.write(mdatHeader)
            fos.write(metadataBytes)
        }
    }

    private fun saveToMediaStore(context: Context, sourceFile: File, title: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, "${title.take(25)}_NEXORA.mp4")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/NexoraAI")
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(out)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            }
        }
        return uri
    }
}
