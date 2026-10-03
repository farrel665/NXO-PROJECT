package com.example.imguioverlay
object NativeBridge {
    init { System.loadLibrary("imgui_overlay") }
    external fun init()
    external fun resize(width:Int, height:Int)
    external fun render(): Boolean
    external fun touch(action:Int, x:Float, y:Float, down:Boolean)
}
