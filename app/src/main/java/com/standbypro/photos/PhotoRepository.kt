package com.standbypro.photos

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PhotoRepository(private val context: Context) {
    // In a real app this would query MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    
    fun getPhotos(): Flow<List<String>> = flow {
        // Mock list of local resource URIs or content URIs
        emit(emptyList())
    }
}
