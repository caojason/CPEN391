#include <stdio.h>

#define WIDTH 1920
#define HEIGHT 1080
#define DEPTH 3

volatile unsigned *gaussian_acc = (volatile unsigned *)0xff202040; //gaussian filter acceletaor base address
volatile unsigned *gaussian_output = (volatile unsigned *)0x1080; //the output value of the accelerator
volatile unsigned *compression_acc = (volatile unsigned *)0x10c0; //compression accelerator base address
volatile unsigned *compression_out = (volatile unsigned *)0x10f0; //output of the compression accelerator


void apply_gaussian(unsigned char input[HEIGHT][WIDTH][DEPTH], unsigned char output[HEIGHT][WIDTH][DEPTH]) {
    for (int i = 0; i < HEIGHT; i++) {
        for (int j = 0; j < WIDTH; j++) {
            for (int d = 0; d < DEPTH; d++) {
                int top = (i == 0) ? 0 : i - 1;
                int bottom = (i == HEIGHT - 1) ? (HEIGHT - 1) : (i + 1);
                int left = (j == 0) ? 0 : j - 1;
                int right = (j == WIDTH - 1) ? (WIDTH - 1) : (j + 1);
                *(gaussian_acc + 1) = input[top][left][d];
                *(gaussian_acc + 2) = input[top][j][d];
                *(gaussian_acc + 3) = input[top][right][d];
                *(gaussian_acc + 4) = input[i][left][d];
                *(gaussian_acc + 5) = input[i][j][d];
                *(gaussian_acc + 6) = input[i][right][d];
                *(gaussian_acc + 7) = input[bottom][left][d];
                *(gaussian_acc + 8) = input[bottom][j][d];
                *(gaussian_acc + 9) = input[bottom][right][d];

                output[i][j][d] = *gaussian_output;  //place the output in the correct memory address
            }
        }
    }
}

void compression(unsigned char input[HEIGHT][WIDTH][DEPTH]) {
    *(compression_acc + 1) = &input[0][0][0];
    *(compression_acc) = 0;
    *compression_acc; //make sure compression is finished

    //the hardware should place the output starting from the address compression_out
    //the first four bytes should be the size in words?
    //the first two bytes should be width in big endian
    //the second two bytes shoule be length in big endian
}

int main() {

    //read the image input from the bluetooth file 
    FILE* fp;

    unsigned char img[HEIGHT][WIDTH][DEPTH];
    fp = fopen("bytes.txt", "r+");

    for (int i = 0; i < HEIGHT; i++) {
        for (int j = 0; j < WIDTH; j++) {
            for (int k = 0; k < DEPTH; k++) {
                char buff;
                img[i][j][k] = fgetc(fp);
            }
        }
    }

    unsigned char out[HEIGHT][WIDTH][DEPTH];

    apply_gaussian(img, out);

    compression(out);

    //write the compressed image to the WiFi port
    FILE* newFp;
    newFp = fopen("out.txt", "w+");
    
    fwrite((const void *)(compression_out + 1), sizeof(compression_out), (size_t)(*(compression_out)), newFp);

    fflush(newFp);
    fclose(fp);
    fclose(newFp);
    return 0;
}