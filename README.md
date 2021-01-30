# CPEN391

## some notes for image transferring

- in python after you get an image
    ```
    from PIL import Image
    import numpy

    img = Image.open("img.png")
    img_arr = numpy.array(img)
    # directly send the byte stream onto the internet connection
    ```

- in C when we receive the image (**should be a full array**)
    ```
    #include <stdio.h>

    int main() {
        FILE* fp;
        int width = 636;
        int height = 636;
        int depth = 4;
        char img[height][width][depth];
        fp = fopen("bytes.txt", "r+");

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                for (int k = 0; k < depth; k++) {
                    char buff;
                    img[i][j][k] = fgetc(fp);
                }
            }
        }
    }
    ```
- should be able to send the char array directly to the server after processing
- from server to mobile end or mobile end to server, use base64 encoding