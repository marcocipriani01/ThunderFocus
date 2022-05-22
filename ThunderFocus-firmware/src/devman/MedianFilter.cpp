#include "MedianFilter.h"

MedianFilter::MedianFilter(int window) {
    _buf = new double[window];
    _usableWindow = 0;
    _window = window;
    _median = NAN;
}

double MedianFilter::add(double item) {
    if ((!isnan(item)) && (!isinf(item))) {
        if (_usableWindow < _window) {
            // Append
            _buf[_usableWindow] = item;
            _usableWindow++;
        } else {
            // Shift & append
            for (int i = 1; i < _window; i++) {
                _buf[i - 1] = _buf[i];
            }
            _buf[_window - 1] = item;
        }
        _median = median(_buf, _usableWindow);
    }
    return _median;
}

double MedianFilter::get() { return _median; }

boolean MedianFilter::isReady() { return (_usableWindow >= _window); }

double MedianFilter::median(double* in, int n) {
    switch (n) {
        case 1:
            return in[0];

        case 2:
            return (in[0] + in[1]) / 2.0;

        case 3: {
            double a = in[0], b = in[1], c = in[2];
            if ((a <= b) && (a <= c)) return (b <= c) ? b : c;
            if ((b <= a) && (b <= c)) return (a <= c) ? a : c;
            return (a <= b) ? a : b;
        }

        default: {
            double x[n];
            // Modified insertion sort
            for (int i = 0; i < n; i++) {
                int j = i - 1;
                while ((j >= 0) && (x[j] > in[i])) {
                    x[j + 1] = x[j];
                    j--;
                }
                x[j + 1] = in[i];
            }
            if ((n % 2) == 0) {
                // If there is an even number of elements, return mean of the two elements in the middle
                return (x[n / 2] + x[n / 2 + 1]) / 2.0;
            } else {
                // Else return the element in the middle
                return x[n / 2];
            }
        }
    }
}