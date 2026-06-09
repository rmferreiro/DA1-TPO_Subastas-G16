import os
import re

xml_files = [
    "app/src/main/res/layout/activity_detalle_subasta.xml",
    "app/src/main/res/layout/activity_home.xml",
    "app/src/main/res/layout/activity_lista_subastas.xml",
    "app/src/main/res/layout/activity_mis_pujas.xml",
    "app/src/main/res/layout/activity_notificacion_ganador.xml",
    "app/src/main/res/layout/activity_permanecer_subasta.xml",
    "app/src/main/res/layout/activity_subasta_en_vivo.xml",
    "app/src/main/res/layout/bottom_nav.xml"
]

for f in xml_files:
    if os.path.exists(f):
        with open(f, "r", encoding="utf-8") as file:
            content = file.read()
        
        # We want to keep HEAD.
        # Regex to find <<<<<<< HEAD ... ======= ... >>>>>>> ca6dc...
        new_content = re.sub(r"<<<<<<< HEAD\n(.*?)\n=======\n.*?\n>>>>>>>[^\n]*\n", r"\1\n", content, flags=re.DOTALL)
        
        with open(f, "w", encoding="utf-8") as file:
            file.write(new_content)
        print(f"Fixed {f} keeping HEAD.")

# Fix build.gradle
if os.path.exists("app/build.gradle"):
    with open("app/build.gradle", "r", encoding="utf-8") as file:
        content = file.read()
    merged_gradle = """    implementation 'com.auth0.android:jwtdecode:2.0.2'
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'"""
    new_content = re.sub(r"<<<<<<< HEAD\n.*?\n=======\n.*?\n>>>>>>>[^\n]*\n", merged_gradle + "\n", content, flags=re.DOTALL)
    with open("app/build.gradle", "w", encoding="utf-8") as file:
        file.write(new_content)
    print("Fixed build.gradle")

# Fix AndroidManifest.xml
if os.path.exists("app/src/main/AndroidManifest.xml"):
    with open("app/src/main/AndroidManifest.xml", "r", encoding="utf-8") as file:
        content = file.read()
    # We will just replace the conflict markers and carefully combine.
    # Actually, it's easier to keep HEAD and then insert the new activities if they don't exist.
    # Let's just keep HEAD for the whole file and then we can manually add LoginActivity.
    new_content = re.sub(r"<<<<<<< HEAD\n(.*?)\n=======\n.*?\n>>>>>>>[^\n]*\n", r"\1\n", content, flags=re.DOTALL)
    with open("app/src/main/AndroidManifest.xml", "w", encoding="utf-8") as file:
        file.write(new_content)
    print("Fixed AndroidManifest.xml keeping HEAD")

# Fix SplashActivity
if os.path.exists("app/src/main/java/tpo/g16/blackwood/splash/SplashActivity.java"):
    with open("app/src/main/java/tpo/g16/blackwood/splash/SplashActivity.java", "r", encoding="utf-8") as file:
        content = file.read()
    new_content = re.sub(r"<<<<<<< HEAD:.*?\n=======\n.*?\n>>>>>>>[^\n]*\n", "", content, flags=re.DOTALL)
    with open("app/src/main/java/tpo/g16/blackwood/splash/SplashActivity.java", "w", encoding="utf-8") as file:
        file.write(new_content)
    print("Fixed SplashActivity.java")

print("Done resolving automated ones.")
