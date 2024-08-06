rem Spusti prikaz pro sestaveni projektu pomoci gradle wrapperu (bez testu)
.\gradlew.bat build -x test

rem zkopiruje vytvoreny jar soubor do slozky config-editor a prejmenuje ho na NATT.jar
copy app\build\libs\app-all.jar config-editor\NATT.jar
echo NATT.jar moved to config-editor

