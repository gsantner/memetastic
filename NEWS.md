# MemeTastic - News

## General

### Installation
You can install and update from [F-Droid](https://f-droid.org/repository/browse/?fdid=io.github.gsantner.memetastic) or [GitHub](https://github.com/gsantner/memetastic/releases/latest).

F-Droid is a store for free & open source apps.
The *.apk's available for download are signed by the F-Droid team and guaranteed to correspond to the (open source) source code of MemeTastic.
Generally this is the recommended way to install MemeTastic & keep it updated.


### Get informed
* Check the [project readme](https://github.com/gsantner/memetastic/tree/news#readme) for general project information.
* Check the [project news](https://github.com/gsantner/memetastic/blob/master/NEWS.md#readme) for more details on what is going on.
* Check the [project git history](https://github.com/gsantner/memetastic/commits/master) for most recent code changes.

### The right place to ask
If you have questions or found an issue please head to the [MemeTastic project](https://github.com/gsantner/memetastic/issues/new/choose) and ask there. 
[Search](https://github.com/gsantner/memetastic/issues?q=#js-issues-search) for same/similar and related issues/questions before, it might be already answered or resolved.   


### Navigation
* [MemeTastic v1.7 - Rotation gesture for ImageViewer](#memetastic-v17---rotation-gesture-for-imageviewer)
* [MemeTastic v1.6 - Complete offline, Redesign, Meme templates](#memetastic-v16---complete-offline-redesign-meme-templates)


------------------------------------------------------------------------------------------------------------------------------------

------------------------------------------------------------------------------------------------------------------------------------

------------------------------------------------------------------------------------------------------------------------------------


# MemeTastic v1.7 - Rotation gesture for ImageViewer
_30. July 2021_


## List of sites for Meme Templates and Funny Images
MemeTastic contains a list of links to sites with Meme Templates and Funny Images. You can show the list and get to these sites using the »Help« option in the bottom navigation »More« section.

You can suggest sites [here](https://github.com/gsantner/memetastic/issues/126) if you are missing something.


## Privacy: MemeTastic - your true Offline app
MemeTastic has no Internet permission, hence it can't communicate with any network.
There is no tracking, calling-home or picture upload in MemeTastic. 

Use the share button to send edited images to other apps. You can also use any gallery/file manager app to view MemeTastic created images.

## Changelog
- Translate app into more languages
- Make favourite icon in popup smaller
- Disable menu button when not in selection at "create page"
- Add rotation gesture to ImageViewer
- Update list of links to memes & meme templates
- Replace CI/CD with GitHub Actions



------------------------------------------------------------------------------------------------------------------------------------

------------------------------------------------------------------------------------------------------------------------------------

------------------------------------------------------------------------------------------------------------------------------------


# MemeTastic v1.6 - Complete offline, Redesign, Meme templates
_15. September 2019_


## Usability

### Create Memes faster
When you start editing a picture, the editor will now automatically select the top caption.
This means the keyboard shows up immediately and you can start right away to enter text for the top caption.

### Redesign
The app uses a brown-black as primary color now, which improves readability and recognizability of components and texts a lot compared to the previous blue color.

<div style="display:flex; width: 100%; flex-wrap: nowrap; overflow-x: auto; margin-top: 3px;">
	<img src="https://raw.githubusercontent.com/gsantner/memetastic/master/metadata/en-US/phoneScreenshots/01.jpg" style="flex: 0 0 auto; min-width:120px; max-height: 400px;">
	<div style="width: 5px;">&nbsp;</div>
	<img src="https://raw.githubusercontent.com/gsantner/memetastic/master/metadata/en-US/phoneScreenshots/02.jpg" style="flex: 0 0 auto; min-width:120px; max-height: 400px;" >
	<div style="width: 5px;">&nbsp;</div>
	<img src="https://raw.githubusercontent.com/gsantner/memetastic/master/metadata/en-US/phoneScreenshots/03.jpg" style="flex: 0 0 auto; min-width:120px; max-height: 400px;" >
	<div style="width: 5px;">&nbsp;</div>
	<img src="https://raw.githubusercontent.com/gsantner/memetastic/master/metadata/en-US/phoneScreenshots/04.jpg" style="flex: 0 0 auto; min-width:120px; max-height: 400px;">
	<div style="width: 5px;">&nbsp;</div>
</div>


### Apply Text settings to all captions
This is a new checkbox option inside the meme editor controls. When checked, all text settings are synced between all captions (size, font, colors etc.). It is checked by default and can be disabled in case you want to have e.g. different sizes for all captions.

### Search replaces Tabs in Meme Template list
Previously the meme template list contained a tabbed view for groups of templates. There is already bottom navigation and search for template tags/categories so tabs were obsole and thus removed.

### Rotation gesture for Image Viewer
The image viewer (created images & original unedited image) now supports a simple rotation gesture. The image viewer now support pinch to zoom, image movement and rotation. 

The rotation happens on 90 degree steps and is applied only for the currently viewed image till closed.

### Use MemeTastic as Image Viewer
There is a new toolbar menu option at meme editor to view the (untouched) original image.

Together with the new [Rotation gesture for Image Viewer](#rotation-gesture-for-image-viewer), you can use MemeTastic as a simple & lightweight picture viewer. (Without saving any images)

The image viewer uses full width & height of the screen and has complete black background.

### List of sites for Meme Templates and Funny Images
MemeTastic now contains a list of links to sites with Meme Templates and Funny Images. You can show the list and get to these sites using the »Help« option in the bottom navigation »More« section.

You can suggest sites [here](https://github.com/gsantner/memetastic/issues/126) if you are missing something.


## Privacy: MemeTastic - your true Offline app
MemeTastic has no Internet permission, hence it can't communicate with any network.
There is no tracking, calling-home or picture upload in MemeTastic. 

Use the share button to send edited images to other apps. You can also use any gallery/file manager app to view MemeTastic created images.

(*This info was not included in any update note yet.*)
