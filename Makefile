all: wserve

wserve: wserve.cpp
	g++ wserve.cpp -o wserve

clean:
	rm wserve