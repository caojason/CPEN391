#include <stdio.h>
#include <math.h>

//use gcc gaussianFilter.c -lm -o gaussianFilter.o
#define HEIGHT 3
#define WIDTH 3
#define DEPTH 3

void kernelGeneration(double k[][3]);
void doGaussian(char img[HEIGHT][WIDTH][DEPTH], double k[][3], char out[HEIGHT][WIDTH][DEPTH]);

int main() {
    FILE* fp;

    char img[HEIGHT][WIDTH][DEPTH];
    fp = fopen("bytes.txt", "r+");

    for (int i = 0; i < HEIGHT; i++) {
        for (int j = 0; j < WIDTH; j++) {
            for (int k = 0; k < DEPTH; k++) {
                char buff;
                img[i][j][k] = fgetc(fp);
            }
        }
    }

    double k[3][3];
    kernelGeneration(k);

    char out[HEIGHT][WIDTH][DEPTH];

    doGaussian(img, k, out);

    FILE* newFp;
    newFp = fopen("out.txt", "w+");
    for (int i = 0; i < HEIGHT; i++) {
        for (int j = 0; j < WIDTH; j++) {
            for (int k = 0; k < DEPTH; k++) {
                fputc(out[i][j][k], newFp);
            }
        }
    }
    fflush(newFp);
    fclose(fp);
    fclose(newFp);
    return 0;
}

void kernelGeneration(double k[][3]) {
    double stdv = 1.0;
    double r_sqr;
    double s = 2.0 * stdv * stdv;
    //each value is 1/(2PI*stdv^2)e^((-r_sqr)/(2stdv^2))
    //where r_sqr = x^2+y^2 or row^2+col^2

    double sum = 0.0; //used for normalization

    for (int row = -1; row <=1; row++) {
        for (int col = -1; col <= 1; col++) {
            r_sqr = row*row + col*col;
            k[row+1][col+1] = exp(-r_sqr/s)/(M_PI * s);
            sum += k[row+1][col+1];
        }
    }
    
    //normalize the kernel
    for (int row = 0; row < 3; row++) {
        for (int col = 0; col < 3; col++) {
            k[row][col] /= sum;
        }
    }
}

void doGaussian(char img[HEIGHT][WIDTH][DEPTH], double k[][3], char out[HEIGHT][WIDTH][DEPTH]) {
    
    for (int i = 0; i < HEIGHT; i++) {
        for (int j = 0; j < WIDTH; j++) {
            for (int d = 0; d < DEPTH; d++) {
                //because the filter is double, we need to calculate the double value first and convert back to char
                double sum = 0.0;
                for (int kr = -1; kr <= 1; kr++) {
                    for (int kc = -1; kc <=1; kc++) {
                        //make sure the row and the col are within the data range
                        //this is equivalent to extending around the image by the edge value
                        int pixelRow = (i+kr < 0) ? 0 : ((i+kr >= HEIGHT) ? HEIGHT - 1 : i+kr); 
                        int pixelCol = (j+kc < 0) ? 0 : ((j+kc >= WIDTH) ? WIDTH - 1 : j+kc);
                        sum += k[kr+1][kc+1] * img[pixelRow][pixelCol][d];
                    }
                }
                //firstly, cast the sum to an int, then and with 255 for a valid RGBA value, then turn it into a char (1 byte)
                sum = round(sum);
                out[i][j][d] = (char)((0xFF) & (int)sum);
            }
        }
    }
}