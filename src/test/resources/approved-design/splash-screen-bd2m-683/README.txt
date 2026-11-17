Approved Figma design for BD2M-683 SC_01_TC_020 (splash screen matches the approved Figma design).

Put one PNG export of the splash screen frame per target device here, with the platform in the
file name:
  splash-screen_android.png   Samsung Galaxy S24 Ultra frame, 384 px wide (or 768 at 2x, 1152 at 3x)
  splash-screen_ios.png       iPhone 17 Pro frame, 402 px wide (or 804 at 2x, 1206 at 3x)

In Figma: select the frame > Export > PNG, any scale - the test reads the scale from the width.
The test shows the app at the same size, compares everything below the moving image grid, and
tries each carousel slide, so any slide may be exported. SC_01_TC_020 is SKIPPED while there is
no file for the platform.
