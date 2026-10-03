#include <GLES3/gl3.h>
#include <android/log.h>
#include "imgui.h"
#include "backends/imgui_impl_opengl3.h"
#include <jni.h>

static bool g_ready=false, g_demo=false, g_enabled=true, g_hide=false;
static float g_value=.75f; static int g_tab=0;
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,"ImGuiOverlay",__VA_ARGS__)

extern "C" JNIEXPORT void JNICALL Java_com_example_imguimenu_ImGuiOverlayView_nativeInit(JNIEnv*,jobject){
    if(g_ready) return;
    IMGUI_CHECKVERSION(); ImGui::CreateContext();
    ImGuiIO& io=ImGui::GetIO(); io.IniFilename=nullptr;
    io.BackendPlatformName="android-custom"; io.BackendRendererName="imgui_impl_opengl3";
    ImGui::StyleColorsDark(); ImGuiStyle& s=ImGui::GetStyle();
    s.WindowRounding=16.f; s.ChildRounding=12.f; s.FrameRounding=8.f; s.PopupRounding=10.f;
    s.WindowPadding=ImVec2(18,18); s.ItemSpacing=ImVec2(10,10); s.FramePadding=ImVec2(12,9);
    ImGui_ImplOpenGL3_Init("#version 300 es"); g_ready=true;
}
extern "C" JNIEXPORT void JNICALL Java_com_example_imguimenu_ImGuiOverlayView_nativeResize(JNIEnv*,jobject,jint w,jint h){ if(g_ready) ImGui::GetIO().DisplaySize=ImVec2((float)w,(float)h); glViewport(0,0,w,h); }
extern "C" JNIEXPORT void JNICALL Java_com_example_imguimenu_ImGuiOverlayView_nativeTouch(JNIEnv*,jobject,jint action,jfloat x,jfloat y){
    if(!g_ready) return; auto& io=ImGui::GetIO(); io.AddMousePosEvent(x,y);
    if(action==0) io.AddMouseButtonEvent(0,true); else if(action==1||action==3) io.AddMouseButtonEvent(0,false);
}
static void UI(){
    ImGuiIO& io=ImGui::GetIO();
    ImGui::SetNextWindowPos(ImVec2(0,0),ImGuiCond_Always); ImGui::SetNextWindowSize(io.DisplaySize,ImGuiCond_Always);
    ImGui::Begin("##root",nullptr,ImGuiWindowFlags_NoDecoration|ImGuiWindowFlags_NoMove|ImGuiWindowFlags_NoSavedSettings);
    if(io.DisplaySize.x<300){ ImGui::SetCursorPos(ImVec2(22,28)); ImGui::TextColored(ImVec4(.55f,.4f,1.f,1.f),"●"); ImGui::SameLine(); ImGui::Text("IMGUI"); ImGui::TextDisabled("Tap to open"); ImGui::End(); return; }
    ImGui::TextColored(ImVec4(.58f,.43f,1.f,1.f),"IMGUI"); ImGui::SameLine(); ImGui::Text("OVERLAY MENU");
    ImGui::SameLine(io.DisplaySize.x-130); if(ImGui::Button("Hide",ImVec2(100,38))) g_hide=true;
    ImGui::Separator();
    const char* tabs[]={"Main","Visual","Settings"};
    for(int i=0;i<3;i++){ if(i) ImGui::SameLine(); if(ImGui::Button(tabs[i],ImVec2(110,40))) g_tab=i; }
    ImGui::Spacing();
    if(g_tab==0){
        ImGui::Text("General"); ImGui::Separator();
        ImGui::Checkbox("Feature enabled",&g_enabled); ImGui::SliderFloat("Intensity",&g_value,0.f,1.f,"%.2f"); ImGui::Checkbox("ImGui demo",&g_demo);
        ImGui::TextWrapped("These controls are placeholders for your own application logic. No game-specific modification code is included.");
    } else if(g_tab==1){
        ImGui::Text("Visual"); ImGui::Separator(); ImGui::Checkbox("Example overlay",&g_enabled); ImGui::SliderFloat("Opacity",&g_value,.1f,1.f,"%.2f");
    } else {
        ImGui::Text("Settings"); ImGui::Separator();
        if(ImGui::Button("Reset settings",ImVec2(160,42))){g_enabled=true;g_value=.75f;}
        ImGui::TextDisabled("Drag the bubble while the menu is hidden.");
    }
    ImGui::End(); if(g_demo) ImGui::ShowDemoWindow(&g_demo);
}
extern "C" JNIEXPORT void JNICALL Java_com_example_imguimenu_ImGuiOverlayView_nativeRender(JNIEnv*,jobject){
    if(!g_ready) return; auto& io=ImGui::GetIO(); io.DeltaTime=1.f/60.f;
    glClearColor(0.025f,0.03f,0.045f,0.f); glClear(GL_COLOR_BUFFER_BIT);
    ImGui::NewFrame(); UI(); ImGui::Render(); ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
}
extern "C" JNIEXPORT jboolean JNICALL Java_com_example_imguimenu_ImGuiOverlayView_nativeConsumeHideRequest(JNIEnv*,jobject){ bool v=g_hide; g_hide=false; return v; }
extern "C" JNIEXPORT void JNICALL Java_com_example_imguimenu_ImGuiOverlayView_nativeDestroy(JNIEnv*,jobject){ if(!g_ready) return; ImGui_ImplOpenGL3_Shutdown(); ImGui::DestroyContext(); g_ready=false; }
