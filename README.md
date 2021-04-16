# Torch

## Hardware part  

### Altera DE1-SoC 
- Image denoising accelerator using Gaussian Filter
- Bluetooth connection with Raspberry Pi
  - receiving image from the Pi
  - respond with denoised image

### Raspberry Pi
- Take pictures using Pi Camera
- Bluetooth connection with DE1-SoC to transmit images
- image compression and base 64 encoding
- WiFi connection with the backend

## Serverside
- Receive images from the DE1-SoC
  - Identify the owner by MAC Address sent together with the encoded image as JSON
- Convert the images to videos when a certain number of images for a location is received(20)
- Use OpenCV to count the number of people in the video stream, and update the SQL database accordingly
- Communicate with the mobile frontend

## Mobile app
- Login 
  - Google sign in
  - Update/Retrieve data from the server user database
- Favorite
  - Record the stores saved by the user
  - Go into detailed page for each store
- Pairing
  - Bluetooth pairing with DE1-SoC to identify the owner of the store
  - phone GPS recording the store location
  - Update the user and store database
- Browsing
  - Google map integration
  - list of stores that are not owned by the user
  - User can add a store to favorites from here
- Details
  - Google map integration for the specific store
  - Average daily customer volume chart 
  - Image and detailed analysis for owner and owner-approved customers
- Letter
  - Send letter through backend to request for permissions
