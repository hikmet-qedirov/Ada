package com.solidict.ada.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solidict.ada.model.video.Video
import com.solidict.ada.repositories.VideoRepository
import com.solidict.ada.util.Resource
import com.solidict.ada.util.SaveDataPreferences
import com.solidict.ada.util.makeMultiPartBodyPart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

private const val TAG = "TestVideoViewModel"

@HiltViewModel
class VideoViewModel
@Inject
constructor(
    private val videoRepository: VideoRepository,
    private val saveDataPreferences: SaveDataPreferences,
) : ViewModel() {
    // video post
    private var _videoPost: MutableLiveData<Resource<Video>> = MutableLiveData()
    val videoPost: LiveData<Resource<Video>> get() = _videoPost

    fun videoPost() = viewModelScope.launch {
        _videoPost.value = null
        val token = saveDataPreferences.readToken()!!
        val videoId = saveDataPreferences.readVideoId()
        val fileUri = saveDataPreferences.readVideoUri()!!
        val filePart = makeMultiPartBodyPart(fileUri)
        Log.d(TAG, "videoPost token :: $token")
        Log.d(TAG, "videoPost videoId :: $videoId")
        Log.d(TAG, "videoPost videoId :: $fileUri")
        Log.d(TAG, "videoPost filePart :: $filePart")
        try {
            _videoPost.value = Resource.Loading()
            if (videoId.isNullOrEmpty()) {
                val response = videoRepository.videoPost(
                    token,
                    filePart
                )
                _videoPost.value = handleVideoResponse(response)
                Log.d(
                    TAG,
                    """
                fun videoPost response :::
                $response"""
                )
            } else {
                val response = videoRepository.videoPostWithVideoId(
                    token,
                    filePart,
                    videoId.toInt()
                )
                _videoPost.value = handleVideoResponse(response)
                Log.d(
                    TAG,
                    """
                fun videoPostWithId response :::
                $response
                """
                )
            }
        } catch (e: IOException) {
            Log.d(TAG, "videoPost IOException :: ${e.message}")
            _videoPost.value = Resource.Error(e.message!!)
        } catch (e: HttpException) {
            Log.d(TAG, "videoPost IOException :: ${e.message()}")
            _videoPost.value = Resource.Error(e.message!!)
        }
    }

    private fun handleVideoResponse(response: Response<Video>): Resource<Video> {
        if (response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it)
            }
        }
        return Resource.Error(response.message())
    }


}