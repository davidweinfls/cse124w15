all: wserve

wserve: wserve.cpp
	g++ -g wserve.cpp -o wserve

clean:
	rm wserve
