/*
	Model of the cylinder used for the H-Lock project.
	NOT TO BE PRINTED!
	AUTHOR: Albert Viaplana Ventura
	Network Embedded Systems
	TU Wien 2015
*/

// The knob of the cylinder. Removable.
module knob()
{
	union()
	{
		cylinder(h = 12.6, d = 33.88);
		translate([0, 0, 12.6])
		cylinder(h = 19.6, d = 33.8, d2 = 16.4);
		translate([0, 0, 32.2])
		difference() 
		{
			cylinder(h = 4.2, d = 16.4, d = 16.4);

			// Hole that is going to secure the knob
			translate([0, 0, 1.85])
			rotate(90, [0, 1, 0]) 
			cylinder(h = 16.4, d = 2.3, center = true);
		}
	}
}


// Main cylinder.
module cylinder_lock()
{
	difference()
	{
		union()
		{
			cylinder(d=10, h=60);
			translate([-5, -25, 0])
			cube([10, 25, 60]);


			translate([0, -19.5, 0])
			cylinder(d=17, h=60);


			translate([0,-19.5,59.6])
			rotate(90,[0,0,1])
			{
				difference()
				{
					// Bar that is connecting the knob with the cylinder
					cylinder(d=13, h=16);
					rotate(90, [0, 1, 0])
					
					// Hole that is going to fix the gear.
					translate([-4.55, 0, -8])
					cylinder(d=2, h=16);
				}
			}
		}

		// Hole for the bolt that is going to secure the cylinder
		translate([-6, -.5, 30])
		rotate(90, [0, 1, 0])
		cylinder(d = 4, h = 12, $fs = 0.5);

		// Wing hole
		translate([-10, -28.1, 25])
		cube([ 20, 24,10]);

	}



	translate([0,-19.5,111.1])
	rotate(90,[0,0,1])
	rotate(180, [0,1,0])
	knob();
}
