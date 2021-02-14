#include <stdio.h>
#include <math.h>

//use gcc imgReader.c -lm -o imgReader.o

void kernelGeneration(double k[][3]);
void doGaussian(char img[636][636][4], int height, int width, int depth, double k[][3], char out[636][636][4]);

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

    double k[3][3];
    kernelGeneration(k);

    char out[height][width][depth];

    doGaussian(img, height, width, depth, k, out);

    FILE* newFp;
    newFp = fopen("out.txt", "w+");
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            for (int k = 0; k < depth; k++) {
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

void doGaussian(char img[636][636][4], int height, int width, int depth, double k[][3], char out[636][636][4]) {
    
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            for (int d = 0; d < depth - 1; d++) {
                //because the filter is double, we need to calculate the double value first and convert back to char
                double sum = 0.0;
                for (int kr = -1; kr <= 1; kr++) {
                    for (int kc = -1; kc <=1; kc++) {
                        //make sure the row and the col are within the data range
                        //this is equivalent to extending around the image by the edge value
                        int pixelRow = (i+kr < 0) ? 0 : ((i+kr >= height) ? height - 1 : i+kr); 
                        int pixelCol = (j+kc < 0) ? 0 : ((j+kc >= width) ? width - 1 : j+kc);
                        sum += k[kr+1][kc+1] * img[pixelRow][pixelCol][d];
                    }
                }
                //firstly, cast the sum to an int, then and with 255 for a valid RGBA value, then turn it into a char (1 byte)
                out[i][j][d] = (char)((0xFF) & (int)sum);
            }
            //the final depth is alpha, we don't need to do gaussian on it
            out[i][j][depth - 1] = img[i][j][depth - 1];
        }
    }
}