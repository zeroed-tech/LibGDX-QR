# Libgdx-QR

LibGDX-QR provides you users with both QR Code generation and QR Code scanning by wrapping several external libraries and native OS APIs.

## Generation
This library uses the QR generation functionality of the Zebra Crossing Barcode library https://github.com/zxing/zxing to generate a bit matrix representing a code then uses https://github.com/earlygrey/shapedrawer to render the QR code to a framebuffer.

Finally, the texture is extracted from the frame buffer and returned.

## Scanning
Currently, Android and IOS support QR Code scanning (there is currently no intention of supporting GWT and Desktop).

### Android
QR scanning on Android is provided by Google's play-services-vision. LibGDX-QR creates a new Activity which implements all QR scanning functionality from this library and returns the first code scanned.

### IOS
QR scanning on IOS is provided by Apple's native AVCapture libraries. LibGDX-QR creates a new View Controller which implements all QR scanning functionality and returns the first code scanned.


# Examples
![Example](https://github.com/zeroed-tech/libgdx-QR/raw/master/Example.png)

# Setup
Add Jitpack if you don't already have it
```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
``` 
Add the dependencies for which ever platforms you are targeting.

**NOTE: The Android and IOS native projects are only required if you want to scan QR codes, if you just want to generate them then only the core project is required.** 
```groovy
project(":core") {
    dependencies {
        implementation 'com.github.zeroed-tech.LibGDX-QR:core:master-SNAPSHOT'
    }
}

project(":android") {
    dependencies {
        implementation 'com.github.zeroed-tech.LibGDX-QR:AndroidNative:master-SNAPSHOT'
    }
}

project(":ios") {
    dependencies {
        implementation 'com.github.zeroed-tech.LibGDX-QR:IOSNative:master-SNAPSHOT'
    }
}
```
## Platform specific setup
Each platform has some extra things that need to be configured get things up and running

### Android
* Change the value of `minSdkVersion` to 16 or higher in `android/build.gradle`
* Add `QRCode.init(new AndroidQRCodeNativeInterface(this));` to `AndroidLauncher.java`

### IOS
* Add `QRCode.init(new IOSQRCodeNativeInterface());` to `IOSLauncher.java`
* Add the following value to `info.plist`
```
<key>NSCameraUsageDescription</key>
<string>Needed to support QR code scanning</string>
```


# Terminology
I haven't read the QR spec so I've almost certainly gotten the terminology wrong in the code. Here is what I've referred to everything as:

* Block - a single square of the QR code
* Eye - the large (7x7) squares in the top left and right and the bottom left corners 

# Usage
Start by creating a QRGenerator and specifying a block size (the number of pixels each square should take up:
```java
QRGenerator generator = new QRGenerator(15)
```

Configure your generator
```java
// Change you block size after you forgot to set it in the constructor
generator.blockSize(10)

// Change the border size. The border is the number of block of white space should be placed around your QR code (0-1 is usually enough)
generator.borderSize(1)

// Set the primary (block) and secondary (background) colours
generator.primaryColor(Color.Black)
generator.secondaryColor(Color.White)

// Set the eye border shape (ARC, CIRCLE or SQUARE)
generator.setEyeBorderShape(QRGenerator.Shape.ARC)

// Set the blocks inside the eyes shape (CIRCLE or SQUARE)
generator.setEyeInnerShape(QRGenerator.Shape.CIRCLE)

// Set the blocks shape (CIRCLE or SQUARE)
generator.setInnerShape(QRGenerator.Shape.SQUARE)
```

Generate your QR code
```java

// Generate your QR code
TextureRegion code = generator.generate("Zeroed.tech")

// Do things with your code, don't forget to dispose of it when you're done
```

## Some example setups
```java
String input = "Zeroed.tech"
 // Generate a basic QR code
new QRGenerator(12).generate(input)

// Generate a QR code with arcs on the eye borders
new QRGenerator(12).setEyeBorderShape(QRGenerator.Shape.ARC).generate(input)

// Generate a QR code with arcs on the eye borders and circular inner bits
new QRGenerator(12).setEyeBorderShape(QRGenerator.Shape.ARC).setEyeInnerShape(QRGenerator.Shape.CIRCLE).generate(input)

// Generate a QR code with arcs on the eye borders and circular everything else
new QRGenerator(12).setEyeBorderShape(QRGenerator.Shape.ARC).setEyeInnerShape(QRGenerator.Shape.CIRCLE).setInnerShape(QRGenerator.Shape.CIRCLE).generate(input)

// Generate a QR code where everything is a circle
new QRGenerator(12).setEyeBorderShape(QRGenerator.Shape.CIRCLE).setEyeInnerShape(QRGenerator.Shape.CIRCLE).setInnerShape(QRGenerator.Shape.CIRCLE).generate(input)

// Change up the colors
new QRGenerator(12).primaryColor(Color.WHITE).secondaryColor(Color.BLACK).generate(input)
new QRGenerator(12).primaryColor(Color.GREEN).secondaryColor(Color.BLACK).generate(input)

// Generate a QR code with a larger border
new QRGenerator(12).borderSize(3).generate(input)

// Generate a larger QR code
new QRGenerator(20).generate(input)
```