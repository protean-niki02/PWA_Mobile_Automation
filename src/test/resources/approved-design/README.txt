Approved design images for the "matches the approved design" test cases.

Put the approved files (exported from Figma / given by the design team) in the folder
for each area. Name every file exactly like the file the app serves, so the test knows
which app image to compare it with.

  launch-icon\    SC_01_TC_002  e.g. Icon-192.png, Icon-512.png, Icon-maskable-192.png, Icon-maskable-512.png
  splash-screen\  SC_01_TC_004  e.g. bima_sugam_logo.png (HTML splash) and images shown on #/splash
  grid-tiles\     SC_01_TC_008  e.g. person_one.jpeg ... person_twentynine.jpeg

PNG/JPEG/GIF are compared pixel by pixel (up to 1% of pixels may differ, for export noise),
so the approved file must have the same width and height as the app's file.
SVG/WebP files must be identical byte for byte.

While a folder has no images, its test case is reported as SKIPPED, not passed.

Sprint 2 - 4 Wheeler - Review Car & Previous Policy Fetched Details:

  review-car-details\        SC_01_TC_022  android.png and ios.png - the approved review screen, exported
                                           at the same pixel size as the test's screenshot of that device
  review-car-details-sizes\  SC_01_TC_023  one approved review screen per size, named <width>x<height>.png
                                           in CSS pixels (e.g. 360x740.png, 412x915.png, 430x932.png)

Design screens are compared with up to 5% of pixels allowed to differ.
