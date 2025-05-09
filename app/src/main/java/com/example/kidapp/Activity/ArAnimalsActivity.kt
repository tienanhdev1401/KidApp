package com.example.kidapp.Activity

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.isGone
import com.example.kidapp.R
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.AugmentedImageNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.material.setExternalTexture
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.VideoNode

class ArAnimalsActivity : AppCompatActivity() {

    private lateinit var sceneView: ArSceneView
    lateinit var placeButton: ExtendedFloatingActionButton
    private lateinit var modelNode: ArModelNode
    private lateinit var animalNameText: TextView
    private var animalName: String = "dog" // Default animal
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_animals)

        // Get animal name from intent
        animalName = intent.getStringExtra("ANIMAL_NAME") ?: "dog"
        
        animalNameText = findViewById(R.id.animal_name)
        animalNameText.text = animalName.capitalize()

        sceneView = findViewById<ArSceneView?>(R.id.sceneView).apply {
            this.lightEstimationMode = Config.LightEstimationMode.DISABLED
        }
        
        placeButton = findViewById(R.id.place)
        placeButton.setOnClickListener {
            placeModel()
        }
        
        // Back button
        val backButton = findViewById<FloatingActionButton>(R.id.btn_back)
        backButton.setOnClickListener {
            finish()
        }

        // Initialize model based on animal name
        setupAnimalModel()
    }

    private fun setupAnimalModel() {
        // Map animal names to their model files
        val modelFile = when(animalName) {
            "elephant" -> "models/elephant.glb"
            "lion" -> "models/lion.glb"
            "monkey" -> "models/monkey.glb"
            "giraffe" -> "models/giraffe.glb"
            "sheep" -> "models/sheep.glb"
            "squirell" -> "models/squirrel.glb"
            "panda" -> "models/panda.glb"
            "kangaroo" -> "models/kangaroo.glb"
            else -> "models/dog.glb" // Default model
        }
        
        // Create and load the model
        modelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = modelFile,
                scaleToUnits = 1f,
                centerOrigin = Position(-0.5f)
            ) {
                sceneView.planeRenderer.isVisible = true
            }
            onAnchorChanged = {
                placeButton.isGone = it != null
            }
        }
        sceneView.addChild(modelNode)
        
        // Play animal sound if available
        playAnimalSound()
    }
    
    private fun playAnimalSound() {
        val soundResId = resources.getIdentifier(
            animalName + "_sound", 
            "raw", 
            packageName
        )
        
        if (soundResId != 0) {
            mediaPlayer = MediaPlayer.create(this, soundResId)
            mediaPlayer?.start()
        }
    }

    private fun placeModel(){
        modelNode.anchor()
        sceneView.planeRenderer.isVisible = false
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}