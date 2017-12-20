# Cordova NFCV (Library RFID Tags) plugin

This plugin lets your app scan a library RFID tag (NFCV) and get the barcode number encoded inside.

## Contents
* [Installation](#installation)
* [Usage](#usage)
* [Credits](#credits)

## Installation
### PhoneGap Build
Include

```
<plugin name="cordova-plugin-librarytag" spec="~1.0.0"/>
```


in the `config.xml` of your application.

### Cordova CLI
```
cordova plugin add cordova-plugin-librarytag
```

## Usage
### Events
This plugin provides an event listener. In your code, add:

```javascript
document.addEventListener('libraryTagDiscovered', (evt) => {
    console.log(evt["tag"][0]);
});
```

### Methods
All methods provided can be found via `nfcv`. If you are using TypeScript, you'll need to add this line on top of each file where you intend to access the plugin:

```typescript
declare const nfcv;
```

Available methods:
* [nfcv.enabled](#nfcvenabled)

#### nfcv.enabled
`nfcv.enabled(success, failure)`

Checks for availability of NFC on device. If success callback is called, NFC is available and enabled. If failure callback is called instead, there is string parameter available telling whether device does have no NFC at all (`NO_NFC`) or if it just disabled (`NFC_DISABLED`).

##### Example
```javascript
nfcv.enabled(() => alert('NFC enabled'), status => alert(status));
```


## Credits
This app was based on [Cordova MiFare Ultralight Plugin](https://github.com/RoopeHakulinen/cordova-plugin-mifare-ultralight).
