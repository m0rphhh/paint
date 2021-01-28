package sample

import javafx.beans.binding.Bindings
import javafx.beans.binding.When
import javafx.beans.property.SimpleDoubleProperty
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Point2D
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.input.*
import javafx.scene.layout.AnchorPane
import javafx.scene.shape.CubicCurve
import javafx.stage.FileChooser
import java.io.File
import java.io.IOException
import java.lang.Integer.parseInt
import java.util.*

var stateAddLink = DataFormat("linkAdd")
var stateAddNode = DataFormat("nodeAdd")

enum class BlueprintMode {
    SetImage,
    ChangeSaturation,
    SetNegative,
    AddSepia,
    Sharpness,
    VertFlip,
    GorFlip,
    BothFlip
}

class DraggableNode : AnchorPane() {

    @FXML
    var left_link_handle: AnchorPane? = null

    @FXML
    var right_link_handle: AnchorPane? = null

    @FXML
    var title_bar: Label? = null

    @FXML
    var ChangeImagePath: Button? = null

    @FXML
    var FirstParam: TextField? = null

    @FXML
    var SecondParam: TextField? = null

    @FXML
    var ThirdParam: TextField? = null

    @FXML
    var DeleteTemplate: Button? = null

    lateinit var contextDragOver: EventHandler<DragEvent>
    lateinit var contextDragDropped: EventHandler<DragEvent>

    lateinit var linkDragDetected: EventHandler<MouseEvent>
    lateinit var linkDeleteDragDetected: EventHandler<MouseEvent>
    lateinit var linkDragDropped: EventHandler<DragEvent>
    lateinit var contextLinkDragOver: EventHandler<DragEvent>
    lateinit var contextLinkDagDropped: EventHandler<DragEvent>

    var myLink = NodeLink()
    var offset = Point2D(0.0, 0.0)

    var modeAction: BlueprintMode = BlueprintMode.SetImage

    var imagePath: String = ""

    var nextActionNode: DraggableNode? = null
    var prevActionNode: DraggableNode? = null

    var superParent: AnchorPane? = null

    @FXML
    private fun initialize() {
        nodeHandlers()
        linkHandlers()
        interactions()

        left_link_handle?.onDragDetected = linkDeleteDragDetected
        left_link_handle?.onDragDropped = linkDragDropped
        right_link_handle?.onDragDetected = linkDragDetected

        parentProperty().addListener{ o, old, new -> superParent = parent as AnchorPane}
    }

    fun doAction(){

        when (modeAction){
            BlueprintMode.SetImage -> {
                ImageEditor.SetImage(imagePath);
            }

            BlueprintMode.ChangeSaturation -> {
                ImageEditor.changeSaturation(parseInt(FirstParam?.text), parseInt(SecondParam?.text), parseInt(ThirdParam?.text))
            }
            BlueprintMode.SetNegative -> {
                ImageEditor.setNegative()
            }
            BlueprintMode.AddSepia -> {
                ImageEditor.AddSepia()
            }
            BlueprintMode.Sharpness -> {
                ImageEditor.Sharpness()
            }
            BlueprintMode.VertFlip -> {
                ImageEditor.VertFlip()
            }
            BlueprintMode.GorFlip -> {
                ImageEditor.GorFlip()
            }
            BlueprintMode.BothFlip -> {
                ImageEditor.BothFlip()
            }
        }
    }

    fun updatePoint(p: Point2D) {
        var local = parent.sceneToLocal(p)
        relocate(
                (local.x - offset.x),
                (local.y - offset.y)
        )
    }

    fun nodeHandlers() {

        contextDragOver = EventHandler { event ->
            updatePoint(Point2D(event.sceneX, event.sceneY))
            event.consume()
        }

        contextDragDropped = EventHandler { event ->
            parent.onDragDropped = null
            parent.onDragOver = null
            event.isDropCompleted = true
            event.consume()
        }

        title_bar!!.onDragDetected = EventHandler { event->
            parent.onDragOver = contextDragOver
            parent.onDragDropped = contextDragDropped

            offset = Point2D(event.x, event.y)
            updatePoint(Point2D(event.sceneX, event.sceneY))

            val content = ClipboardContent()
            content[stateAddNode] = "node"
            startDragAndDrop(*TransferMode.ANY).setContent(content)
        }

    }

    fun linkHandlers() {

        linkDragDetected = EventHandler { event ->
            parent.onDragOver = null
            parent.onDragDropped = null

            Nodes.nodeDragged = this

            if(this.nextActionNode != null){

                this.nextActionNode?.prevActionNode = null
                this.nextActionNode = null
                ImageEditor.RefreshImage()
                refreshCurves()

                parent.onDragOver = contextLinkDragOver
                parent.onDragDropped = contextLinkDagDropped

                superParent!!.children.add(0, myLink)
                myLink.isVisible = true

                val p = Point2D(this.layoutX + width/2, this.layoutY + height/4)
                myLink.setStart(p)

                val content = ClipboardContent()
                content[stateAddLink] = "link"
                startDragAndDrop(*TransferMode.ANY).setContent(content)
            }
            else {

                parent.onDragOver = contextLinkDragOver
                parent.onDragDropped = contextLinkDagDropped

                superParent!!.children.add(0, myLink)
                myLink.isVisible = true

                val p = Point2D(layoutX + width / 2, layoutY + height / 2)
                myLink.setStart(p)

                val content = ClipboardContent()
                content[stateAddLink] = "link"
                startDragAndDrop(*TransferMode.ANY).setContent(content)
            }

            event.consume()
        }

        linkDeleteDragDetected = EventHandler { event ->
            if(this.prevActionNode == null)
                return@EventHandler

            parent.onDragOver = null
            parent.onDragDropped = null

            Nodes.nodeDragged = this.prevActionNode
            this.prevActionNode?.nextActionNode = null
            this.prevActionNode = null
            ImageEditor.RefreshImage()
            refreshCurves()

            parent.onDragOver = contextLinkDragOver
            parent.onDragDropped = contextLinkDagDropped

            superParent!!.children.add(0, myLink)
            myLink.isVisible = true

            val p = Point2D(Nodes.nodeDragged.layoutX + width/2, Nodes.nodeDragged.layoutY + height/4)
            myLink.setStart(p)

            val content = ClipboardContent()
            content[stateAddLink] = "link"
            startDragAndDrop(*TransferMode.ANY).setContent(content)
            event.consume()

            ImageEditor.RefreshImage()
        }

        linkDragDropped = EventHandler { event ->

            parent.onDragOver = null
            parent.onDragDropped = null

            superParent!!.children.removeAt(0)

            if(this.prevActionNode != null){
                println("Уже соединен")
                return@EventHandler
            }
            else {

                val link = NodeLink()
                link.bindStartEnd(Nodes.nodeDragged, this)
                Nodes.curvesNum++
                superParent!!.children.add(0, link)

                Nodes.nodeDragged.nextActionNode = this
                this.prevActionNode = Nodes.nodeDragged
                ImageEditor.RefreshImage()
                Nodes.nodeDragged = null
            }

            event.isDropCompleted = true
            event.consume()
        }

        contextLinkDragOver = EventHandler { event ->
            event.acceptTransferModes(*TransferMode.ANY)
            if (!myLink.isVisible) myLink.isVisible = true
            myLink.setEnd(Point2D(event.x, event.y))

            event.consume()
        }

        contextLinkDagDropped = EventHandler { event ->
            parent.onDragDropped = null
            parent.onDragOver = null

            myLink.isVisible = false
            superParent!!.children.removeAt(0)
            Nodes.nodeDragged = null

            event.isDropCompleted = true
            event.consume()
        }
    }

    fun refreshCurves(){
        while(Nodes.curvesNum > 0){

            superParent!!.children.removeAt(0)
            Nodes.curvesNum--
        }
        Nodes.pool.forEach {
            if(it.nextActionNode != null){

                val link = NodeLink()
                link.bindStartEnd(it, it.nextActionNode!!)
                Nodes.curvesNum++
                superParent!!.children.add(0, link)
            }
        }
    }


    fun interactions(){

        ChangeImagePath?.setOnAction {
            val fileChooser = FileChooser()
            fileChooser.extensionFilters.addAll(
                    FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.webp")
            )
            val selectedFile: File = fileChooser.showOpenDialog(null)
            if (selectedFile != null) {
                imagePath = selectedFile.toURI().toString().drop(6)

                Nodes.currentImagePath = imagePath

                ImageEditor.RefreshImage()
            }
        }

        DeleteTemplate?.setOnAction {
            var prevTemplate = prevActionNode
            prevTemplate?.nextActionNode = null


            var nextTemplate = nextActionNode
            nextTemplate?.prevActionNode = null

            this.nextActionNode = null
            this.prevActionNode = null

            
            while(Nodes.curvesNum > 0)
            {
                superParent!!.children.removeAt(0)
                Nodes.curvesNum--
            }

            Nodes.pool.forEach {
                if(it.nextActionNode != null) {

                    val link = NodeLink()
                    link.bindStartEnd(it, it.nextActionNode!!)
                    Nodes.curvesNum++
                    superParent!!.children.add(0, link)
                }
            }

            this.isVisible = false
            var counter: Int = 1
            while(counter < Nodes.pool.size){

                if(Nodes.pool[counter].id == this.id){
                    Nodes.pool.remove(Nodes.pool[counter])
                    break
                }
                counter++
            }

            ImageEditor.RefreshImage()
        }


        FirstParam?.textProperty()?.addListener { observable, oldValue, newValue ->
            try {
                FirstParam?.text = parseInt(newValue).toString()
                if(parseInt(FirstParam?.text) > 255)
                    FirstParam?.text = "255"
            }
            catch (e: NumberFormatException){
                FirstParam?.text = "0"
            }
            ImageEditor.RefreshImage()
        }

        SecondParam?.textProperty()?.addListener { observable, oldValue, newValue ->
            try {
                SecondParam?.text = parseInt(newValue).toString()
                if(parseInt(SecondParam?.text) > 255)
                    SecondParam?.text = "255"
            }
            catch (e: NumberFormatException){
                SecondParam?.text = "0"
            }
            ImageEditor.RefreshImage()
        }

        ThirdParam?.textProperty()?.addListener { observable, oldValue, newValue ->
            try {
                ThirdParam?.text = parseInt(newValue).toString()
                if(parseInt(ThirdParam?.text) > 255)
                    ThirdParam?.text = "255"
            }
            catch (e: NumberFormatException){
                ThirdParam?.text = "0"
            }
            ImageEditor.RefreshImage()
        }

    }

    init {
        modeAction = Nodes.prefMode
        var fxmlLoader = FXMLLoader()

        when(modeAction){
            BlueprintMode.ChangeSaturation -> fxmlLoader = FXMLLoader(javaClass.getResource("ChangeSaturationTemplate.fxml"))

            BlueprintMode.SetNegative -> fxmlLoader = FXMLLoader(javaClass.getResource("withoutInputTemplate.fxml"))

            BlueprintMode.AddSepia -> fxmlLoader = FXMLLoader(javaClass.getResource("withoutInputTemplate.fxml"))

            BlueprintMode.Sharpness -> fxmlLoader = FXMLLoader(javaClass.getResource("withoutInputTemplate.fxml"))

            BlueprintMode.SetImage -> fxmlLoader = FXMLLoader(javaClass.getResource("SetImageTemplate.fxml"))

            BlueprintMode.VertFlip -> fxmlLoader = FXMLLoader(javaClass.getResource("withoutInputTemplate.fxml"))

            BlueprintMode.GorFlip -> fxmlLoader = FXMLLoader(javaClass.getResource("withoutInputTemplate.fxml"))

            BlueprintMode.BothFlip -> fxmlLoader = FXMLLoader(javaClass.getResource("withoutInputTemplate.fxml"))
        }

        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<Any>()
        id = Nodes.globalId.toString()
        Nodes.globalId++
        title_bar?.text = modeAction.toString()
    }
}

class NodeLink : AnchorPane() {
    @FXML
    var node_link: CubicCurve? = null

    val offsetX = SimpleDoubleProperty()
    val offsetY = SimpleDoubleProperty()
    val offsetDirX1 = SimpleDoubleProperty()
    val offsetDirX2 = SimpleDoubleProperty()
    val offsetDirY1 = SimpleDoubleProperty()
    val offsetDirY2 = SimpleDoubleProperty()

    @FXML
    private fun initialize() {
        offsetX.set(100.0)
        offsetY.set(50.0)

        offsetDirX1.bind(
                When(node_link!!.startXProperty().greaterThan(node_link!!.endXProperty())).then(-1.0).otherwise(1.0))

        offsetDirX2.bind(
                When(node_link!!.startXProperty().greaterThan(node_link!!.endXProperty())).then(1.0).otherwise(-1.0))

        node_link!!.controlX1Property().bind(Bindings.add(node_link!!.startXProperty(), offsetX.multiply(offsetDirX1)))
        node_link!!.controlX2Property().bind(Bindings.add(node_link!!.endXProperty(), offsetX.multiply(offsetDirX2)))
        node_link!!.controlY1Property().bind(Bindings.add(node_link!!.startYProperty(), offsetY.multiply(offsetDirY1)))
        node_link!!.controlY2Property().bind(Bindings.add(node_link!!.endYProperty(), offsetY.multiply(offsetDirY2)))
    }

    fun setStart(point: Point2D) {
        node_link!!.startX = point.x
        node_link!!.startY = point.y
    }

    fun setEnd(point: Point2D) {
        node_link!!.endX = point.x
        node_link!!.endY = point.y
    }

    fun bindStartEnd(source1: DraggableNode, source2: DraggableNode) {
        node_link!!.startXProperty().bind(Bindings.add(source1.layoutXProperty(), source1.width/2.0))
        node_link!!.startYProperty().bind(Bindings.add(source1.layoutYProperty(), source1.height/2.0))
        node_link!!.endXProperty().bind(Bindings.add(source2.layoutXProperty(), source2.width/2.0))
        node_link!!.endYProperty().bind(Bindings.add(source2.layoutYProperty(), source2.height/2.0))
    }

    init {
        val fxmlLoader = FXMLLoader(
                javaClass.getResource("NodeLink.fxml")
        )
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        try {
            fxmlLoader.load<Any>()
        } catch (exception: IOException) {
            throw RuntimeException(exception)
        }
        id = UUID.randomUUID().toString()
    }
}
