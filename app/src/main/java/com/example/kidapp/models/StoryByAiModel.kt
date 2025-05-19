package com.example.kidapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoryByAiModel(
    var title: String = "",
    var content: String = "",
    var imageUrl: String = "",
    var character: String = "",
    var setting: String = "",
    var item: String = "",
    var englishTitle: String = "",
    var englishContent: String = "",
    var charactersList: ArrayList<String> = ArrayList(),
    var itemsList: ArrayList<String> = ArrayList(),
    var scenes: ArrayList<SceneModel> = ArrayList()
) : Parcelable {

    @Parcelize
    data class SceneModel(
        var vietnameseContent: String = "",
        var imageUrl: String = "",
        var englishContent: String = ""
    ) : Parcelable

    fun addScene(scene: SceneModel) {
        scenes.add(scene)
    }

    fun getSceneCount(): Int {
        return scenes.size
    }

    fun getScene(position: Int): SceneModel? {
        return if (position in 0 until scenes.size) {
            scenes[position]
        } else null
    }
} 