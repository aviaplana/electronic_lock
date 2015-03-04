/*
	Casing enclousure for the H-Lock project.
	AUTHOR: Albert Viaplana Ventura
	Network Embedded Systems
	TU Wien 2015
*/

/* 	
	hall switch:
		4.4
		3.4
		1.5
	
*/


// This is used to hold the nuts of the bolts.
module bolt_hold()
{

	difference()
	{
		cube([10,10,6.5]);
		translate([-1,2,2])
		cube([8,6,4]);
		translate([4, 5, -.7])
		cylinder(d=3, h=4, $fs = .5);
	}
		/*
		// Used to test if the holes are aligned
		translate([4, 5, -70])
		cylinder(d=3, h=200, $fs = .5);
		*/
}


// Holes used to hold the case.
module hole_case()
{
	cube([3.5, 20, 5]);

	translate([1.75,20,5])
	rotate(90, [1,0,0])
	cylinder(d=3.5, h=20, $fs = .5);

	translate([1.75,20,0])
	rotate(90, [1,0,0])
	cylinder(d=3.5, h=20, $fs = .5);
}


// The casing. This is the only part that should be printed!
module case()
{
	union()
	{
		translate([-50, -40, -9])
		difference()
		{
			translate([9, 0, 6])
			cube([85, 220, 37]);

			translate([12, 3, -3])
			cube([79, 214, 43]);
			translate([50,40,25])
			cylinder(h = 19.6, d = 34.5);

			// Hole to place the led holder
			translate([21, 207, 39])
			cylinder(h = 5, d = 8);

			// Hole to place the button
			translate([21, 221, 19])
			rotate(90, [1, 0, 0])
			cylinder(h = 5, d = 7, $fs = 0.5);


// START Case stand holes
			union()
			{
				translate([51.5,210,11])
				hole_case();
			}

			union()
			{
				translate([51.5,-11,11])
				hole_case();
				
			}

			rotate(90, [0,0,1])
			union()
			{
				translate([98.5,-15,11])
				hole_case();
			}

			rotate(90, [0,0,1])
			union()
			{
				translate([98.5,-105,11])
				hole_case();
			}


		}

// END Case stand holes


// START Hall boxes

		difference()
		{
			union()
			{
				difference()
				{
					translate([-2, 0, -2])
					cylinder(d=73, h=34);

					translate([-2, 0, -3])
					cylinder(d=63, h=38);	
				}
				difference() 
				{
					translate([-20.15,-45,-1.9])
					rotate(65, [0,0,1])
					cube([16,19,7.9]);

					translate([-35,-50,-3])
					cube([20,11,10]);				
				}

				rotate(38, [0,0,1])
				translate([-5,30,-.99])
				hall_box();

				translate([-30, -1.5, -.99])
				rotate(90, [0, 0, 1])
				hall_box();

				translate([-20.5, -21.5, -.99])
				rotate(134, [0,0,1])
				hall_box();

				
			}

			translate([-8,-35,-5])
			cube([100,70,38]);	

			translate([-18,-39,-5])
			cube([155,105,38]);	

			translate([-35.7,-38.7,-5])
			rotate(65, [0,0,1])
			cube([34,30,38]);

			translate([-60,25,-5])
			rotate(-65, [0,0,1])
			cube([30,30.5,38]);	

			translate([-14,-28,-5])
			rotate(-65, [0,0,1])
			cube([30,30,38]);

			translate([-12,18,-5])
			rotate(45, [0,0,1])
			cube([30,30,38]);

			translate([-38.3,-35,6])
			cube([30,45,30]);
		}
		
// END hall boxes	


//START Servo hold

		// Left
		translate([-21.8,62.8,7])
		{
			difference()
			{
				cube([11, 5, 25]);

				translate([6.75, 9, 6.15])
				rotate(90, [1, 0, 0])
				cylinder(d=4.5, h=10, $fs = 0.5);

				translate([6.75, 9, 16.85])
				rotate(90, [1, 0, 0])
				cylinder(d=4.5, h=10, $fs = 0.5);
			}
		}

		// Right
		translate([29.9,62.8,7]) 
		{
			difference()
			{
				cube([11, 5, 25]);

				translate([4.35, 9, 6.15])
				rotate(90, [1, 0, 0])
				cylinder(d=4.5, h=10, $fs = 0.5);

				translate([4.35, 9, 16.85])
				rotate(90, [1, 0, 0])
				cylinder(d=4.5, h=10, $fs = 0.5);

				translate([7, -3, -2])
				cube([4, 7, 10]);
			}
		
		}
		translate([-10.9,61.5,28.45])
		cube([41, 36.9, 3]);

// END motor hold


// START nut holders

		translate([6,125,25])
		rotate(90, [0, 0, 1])
		bolt_hold();		

		translate([15,163,25])
		rotate(90, [0, 0, 1])
		bolt_hold();		

		translate([37,125,25])
		rotate(90, [0, 0, 1])
		bolt_hold();		

		translate([-28.45, 59.7, 25])
		rotate(90, [0, 0, 1])
		bolt_hold();	

		translate([-28.45, 148.7, 25])
		rotate(90, [0, 0, 1])
		bolt_hold();	

// END nut holders

	}
}
