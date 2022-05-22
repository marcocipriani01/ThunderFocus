#ifndef MEDIAN_FILTER_H
#define MEDIAN_FILTER_H

#include <Arduino.h>
#include <math.h>

class MedianFilter {
   public:
    MedianFilter(int window);
    double add(double item);
	double get();
    boolean isReady();

   private:
    double median(double* in, int n);
    double* _buf;
	double _median;
    int _window;
	int _usableWindow;
};

#endif