import os
import re
import sys
sys.stdout.reconfigure(encoding='utf-8')

conflict_files = [
    "app/build.gradle",
    "app/src/main/AndroidManifest.xml",
    "app/src/main/java/tpo/g16/blackwood/network/RetrofitClient.java",
    "app/src/main/java/tpo/g16/blackwood/splash/SplashActivity.java",
    "app/src/main/res/layout/activity_detalle_subasta.xml",
    "app/src/main/res/layout/activity_home.xml",
    "app/src/main/res/layout/activity_lista_subastas.xml",
    "app/src/main/res/layout/activity_mis_pujas.xml",
    "app/src/main/res/layout/activity_notificacion_ganador.xml",
    "app/src/main/res/layout/activity_permanecer_subasta.xml",
    "app/src/main/res/layout/activity_subasta_en_vivo.xml",
    "app/src/main/res/layout/bottom_nav.xml"
]

for f in conflict_files:
    if os.path.exists(f):
        print(f"--- {f} ---")
        with open(f, "r", encoding="utf-8") as file:
            content = file.read()
            # find all <<<<<<< HEAD to >>>>>>> ...
            matches = re.finditer(r"<<<<<<< HEAD.*?(=======(?s:.*?))?>>>>>>>[^\n]*\n", content, re.DOTALL)
            for m in matches:
                print(m.group(0))
        print("="*40)
