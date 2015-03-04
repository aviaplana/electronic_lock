/*
	Model of the PCB including the arduino.
	NOT TO BE PRINTED!
	AUTHOR: Albert Viaplana Ventura
	Network Embedded Systems
	TU Wien 2015
*/


// Space reserved for the PCB, arduino and cables.
module pcb()
{
	/*
	// Just the arduino
	cube([18.4, 43.6,4.3]);
	*/

	// With board
	difference()
	{
		cube([28, 100, 19.3]);

		translate([10, -.5, -.5])
		cube([19, 18, 20.3]);

		translate([23.5, 8, -.5])
		cube([5, 35, 20.3]);

		translate([3.55, 94.75,-1])
		cylinder(d=2.5, h=22, $fs = 0.5);

		translate([3.55, 5.75,-1])
		cylinder(d=2.5, h=22, $fs = 0.5);
	}
}