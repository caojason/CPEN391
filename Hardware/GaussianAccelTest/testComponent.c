/*
 * testComponent.c

 *
 *  Created on: Mar 28, 2021
 *      Author: Jason Cao
 */

#include <stdio.h>

#define gaussian_accel_base (volatile int *) 0xFF202040
#define component_factor 1000000.0

int main() {

	//////////////////////////// TEST 1:    output = 5, 
        //
        // img_arr = np.array([
        //     [1, 2, 3],
        //     [4, 5, 6],
        //     [7, 8, 9]
        //     ])

	*(gaussian_accel_base + 1) = 1;
	*(gaussian_accel_base + 2) = 2;
	*(gaussian_accel_base + 3) = 3;
	*(gaussian_accel_base + 4) = 4;
	*(gaussian_accel_base + 5) = 5;
	*(gaussian_accel_base + 6) = 6;
	*(gaussian_accel_base + 7) = 7;
	*(gaussian_accel_base + 8) = 8;
	*(gaussian_accel_base + 9) = 9;

	double output = *(gaussian_accel_base) / component_factor;

	printf("TEST 1: Output is %lf\n", output);

	///////////////////////////////// TEST 2:    output = 14.1881 * 10^6,
        //
        // img_arr = np.array([
        //     [1, 2, 3],
        //     [4, 50, 6],
        //     [7, 8, 9]
        //     ])

	*(gaussian_accel_base + 1) = 1;
	*(gaussian_accel_base + 2) = 2;
	*(gaussian_accel_base + 3) = 3;
	*(gaussian_accel_base + 4) = 4;
	*(gaussian_accel_base + 5) = 50;
	*(gaussian_accel_base + 6) = 6;
	*(gaussian_accel_base + 7) = 7;
	*(gaussian_accel_base + 8) = 8;
	*(gaussian_accel_base + 9) = 9;

	double output = *(gaussian_accel_base) / component_factor;

	printf("TEST 2: Output is %lf\n", output);

	///////////////////////////////// TEST 3:    output = 255
        //
        // img_arr = np.array([
        //     [255, 255, 255],
        //     [255, 255, 255],
        //     [255, 255, 255]
        //     ])

	*(gaussian_accel_base + 1) = 255;
	*(gaussian_accel_base + 2) = 255;
	*(gaussian_accel_base + 3) = 255;
	*(gaussian_accel_base + 4) = 255;
	*(gaussian_accel_base + 5) = 255;
	*(gaussian_accel_base + 6) = 255;
	*(gaussian_accel_base + 7) = 255;
	*(gaussian_accel_base + 8) = 255;
	*(gaussian_accel_base + 9) = 255;

	double output = *(gaussian_accel_base) / component_factor;

	printf("TEST 3: Output is %lf\n", output);

	///////////////////////////////// TEST 4:    output = 105.267847
	        //
	        // img_arr = np.array([
	        //     [15, 51, 100],
	        //     [20, 241, 95],
	        //     [152, 75, 82]
	        //     ])

	*(gaussian_accel_base + 1) = 15;
	*(gaussian_accel_base + 2) = 51;
	*(gaussian_accel_base + 3) = 100;
	*(gaussian_accel_base + 4) = 20;
	*(gaussian_accel_base + 5) = 241;
	*(gaussian_accel_base + 6) = 95;
	*(gaussian_accel_base + 7) = 152;
	*(gaussian_accel_base + 8) = 75;
	*(gaussian_accel_base + 9) = 82;

	double output = *(gaussian_accel_base) / component_factor;

	printf("TEST 4: Output is %lf\n", output);

	///////////////////////////////// TEST 5:    output = 0
        //
        // img_arr = np.array([
        //     [0, 0, 0],
        //     [0, 0, 0],
        //     [0, 0, 0]
        //     ])

	*(gaussian_accel_base + 1) = 0;
	*(gaussian_accel_base + 2) = 0;
	*(gaussian_accel_base + 3) = 0;
	*(gaussian_accel_base + 4) = 0;
	*(gaussian_accel_base + 5) = 0;
	*(gaussian_accel_base + 6) = 0;
	*(gaussian_accel_base + 7) = 0;
	*(gaussian_accel_base + 8) = 0;
	*(gaussian_accel_base + 9) = 0;

	double output = *(gaussian_accel_base) / component_factor;

	printf("TEST 5: Output is %lf\n", output);

	return 0;

}
