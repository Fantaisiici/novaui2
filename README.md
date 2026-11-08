# Nova UI — mod Fabric pour Minecraft 26.3

Redéfinit l'interface de Minecraft (côté client uniquement).

| Zone | Ce qui change |
|---|---|
| Écran titre | Remplacé par `NovaTitleScreen` : fond animé (orbes, étoiles), panneau latéral, menu vertical |
| HUD | Barre d'objets flottante avec halo sur l'emplacement actif, main secondaire, barres de vie / faim (avec valeurs), armure et air en fines lignes, absorption, XP + niveau |
| Tous les menus | Voile dégradé léger et liserés néon (`ScreenEvents`) |
| Widgets | Boutons, champs de texte, sliders, emplacements, fond des menus : textures sombres intégrées au mod |

## Versions ciblées

Minecraft 26.3 · Fabric Loader 0.19.5 · Fabric API 0.165.2+26.3 · Loom 1.18 · Java 25 · Gradle 9.7.1
(Minecraft 26.x n'est plus obfusqué : pas de mappings, plugin `net.fabricmc.fabric-loom`.)

## Compiler

1. Installer le JDK 25.
2. Dans ce dossier, générer le wrapper Gradle (le `.jar` du wrapper n'est pas fourni) :
   `gradle wrapper --gradle-version 9.7.1`
   (ou copier `gradlew`, `gradlew.bat` et `gradle/wrapper/gradle-wrapper.jar` depuis le Fabric Example Mod).
3. `./gradlew build` — le mod est dans `build/libs/novaui-1.0.0.jar`.
4. Tester directement : `./gradlew runClient`.
5. Pour l'installer : copier le jar dans `.minecraft/mods/` avec Fabric API.

## Modifier le thème

- Couleurs et helpers de dessin : `src/main/java/com/novaui/Theme.java`
- Textures des widgets : éditer les couleurs de `tools/generate_textures.py` puis `python3 tools/generate_textures.py`

## Si la compilation échoue

Le code a été écrit d'après la documentation Fabric 26.2 / 26.3 sans pouvoir être compilé dans l'environnement de création. Points les plus susceptibles de demander un ajustement mineur :

- `Theme.bigText` : seule méthode qui utilise la pile de matrices (`graphics.pose()`).
- `NovaHud` : noms des constantes `VanillaHudElements` et appels `item` / `itemDecorations` sur `GuiGraphicsExtractor`.
- `NovaUI` : `ScreenEvents.afterExtract`.
- `NovaTitleScreen` : constructeurs `OptionsScreen`, `SelectWorldScreen`, `JoinMultiplayerScreen`.

## Performance

Optimisations incluses dans Nova UI : bandes de dégradé réduites, disques en bandes avec demi-largeurs en cache, moins d'étoiles animées, chaînes de nombres du HUD pré-calculées (aucune allocation par image dans le HUD).

Nova UI ne touche ni au rendu du monde ni au chargement des chunks. Pour cela, utilise des mods dédiés disponibles pour 26.3 : Sodium (rendu) et Lithium (logique du jeu). Vérifie la compatibilité des autres (C2ME, ModernFix) sur Modrinth avant de les ajouter.
