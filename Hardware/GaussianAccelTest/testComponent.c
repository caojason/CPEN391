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

	printf("Output is %lf\n", output);

	return 0;

}
