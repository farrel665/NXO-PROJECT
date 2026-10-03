#include <jni.h>
#include <GLES3/gl3.h>
#include <android/log.h>
#include "imgui.h"
#include "backends/imgui_impl_opengl3.h"

#define LOG_TAG "ImGuiOverlay"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static bool g_initialized=false;
static int g_width=1, g_height=1;
static bool g_mouse_down=false;
static float g_mouse_x=0, g_mouse_y=0;
static bool g_demo=false;
static bool g_feature_a=false;
static bool g_feature_b=true;
static float g_strength=0.65f;
static int g_tab=0;
static bool g_hide_requested=false;

extern "C" JNIEXPORT void JNICALL Java_com_example_imguioverlay_NativeBridge_init(JNIEnv*, jobject) {
    if(g_initialized) return;
    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO& io=ImGui::GetIO();
    io.ConfigFlags |= ImGuiConfigFlags_NavEnableKeyboard;
    io.DisplaySize=ImVec2((float)g_width,(float)g_height);
    io.Fonts->AddFontDefault();
    ImGui::StyleColorsDark();
    ImGuiStyle& style=ImGui::GetStyle();
    style.WindowRounding=12.0f;
    style.FrameRounding=7.0f;
    style.GrabRounding=7.0f;
    ImGui_ImplOpenGL3_Init("#version 300 es");
    g_initialized=true;
}

extern "C" JNIEXPORT void JNICALL Java_com_example_imguioverlay_NativeBridge_resize(JNIEnv*, jobject, jint w, jint h) {
    g_width=w; g_height=h;
    if(g_initialized) ImGui::GetIO().DisplaySize=ImVec2((float)w,(float)h);
    glViewport(0,0,w,h);
}

extern "C" JNIEXPORT void JNICALL Java_com_example_imguioverlay_NativeBridge_touch(JNIEnv*, jobject, jint action, jfloat x, jfloat y, jboolean down) {
    g_mouse_x=x; g_mouse_y=y;
    if(action==0) g_mouse_down=true;
    else if(action==1 || action==3) g_mouse_down=false;
    else if(action==2) g_mouse_down=(bool)down;
}

static void DrawMenu() {
    ImGui::SetNextWindowPos(ImVec2(10,10), ImGuiCond_Always);
    ImGui::SetNextWindowSize(ImVec2((float)g_width-20.0f,(float)g_height-20.0f), ImGuiCond_Always);
    ImGuiWindowFlags flags=ImGuiWindowFlags_NoCollapse|ImGuiWindowFlags_NoResize|ImGuiWindowFlags_NoMove;
    ImGui::Begin("Overlay Menu", nullptr, flags);
    if(ImGui::Button("MAIN", ImVec2(95,38))) g_tab=0;
    ImGui::SameLine(); if(ImGui::Button("VISUAL", ImVec2(95,38))) g_tab=1;
    ImGui::SameLine(); if(ImGui::Button("SETTINGS", ImVec2(95,38))) g_tab=2;
    ImGui::Separator();
    if(g_tab==0) {
        ImGui::Text("Features");
        ImGui::Checkbox("Feature A", &g_feature_a);
        ImGui::Checkbox("Feature B", &g_feature_b);
        ImGui::SliderFloat("Strength", &g_strength, 0.0f, 1.0f, "%.2f");
        ImGui::Text("Status: %s", g_feature_a ? "enabled" : "idle");
    } else if(g_tab==1) {
        ImGui::Text("Visual settings");
        ImGui::Checkbox("Example overlay", &g_feature_b);
        ImGui::ColorEdit4("Accent", (float*)&ImGui::GetStyle().Colors[ImGuiCol_Button]);
    } else {
        ImGui::TextWrapped("This is a standalone ImGui overlay template. Put your own application logic behind these controls.");
        ImGui::Checkbox("Show ImGui demo", &g_demo);
    }
    ImGui::Separator();
    if(ImGui::Button("Hide menu", ImVec2(-1,42))) g_hide_requested=true;
    ImGui::End();
    if(g_demo) ImGui::ShowDemoWindow(&g_demo);
}

extern "C" JNIEXPORT jboolean JNICALL Java_com_example_imguioverlay_NativeBridge_render(JNIEnv*, jobject) {
    if(!g_initialized) return JNI_FALSE;
    ImGuiIO& io=ImGui::GetIO();
    io.DisplaySize=ImVec2((float)g_width,(float)g_height);
    io.MousePos=ImVec2(g_mouse_x,g_mouse_y);
    io.MouseDown[0]=g_mouse_down;
    io.DeltaTime=1.0f/60.0f;
    ImGui_ImplOpenGL3_NewFrame();
    ImGui::NewFrame();
    DrawMenu();
    ImGui::Render();
    glViewport(0,0,g_width,g_height);
    glClearColor(0.035f,0.035f,0.045f,0.96f);
    glClear(GL_COLOR_BUFFER_BIT);
    ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
    bool requested=g_hide_requested;
    g_hide_requested=false;
    return requested ? JNI_TRUE : JNI_FALSE;
}
