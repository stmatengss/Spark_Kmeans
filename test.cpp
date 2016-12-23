#include <iostream>
#include <algorithm>

using namespace std;

int main() {
	int a[] = {1, 2, 3};
	int b = 2;
	for_each(a, a+3, [b](int& x){x=x+b;});

	for(auto i:a) {
		cout << i <<endl;	
	}
	return 0;
}
