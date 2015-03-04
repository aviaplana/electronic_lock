/*
	Casing enclousure for the H-Lock project.
	AUTHOR: Albert Viaplana Ventura
	Network Embedded Systems
	TU Wien 2015
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


// Box where the hall switches will be allocated
module hall_box()
{
	difference()
		{
		translate([-1, 1, -1])
		cube([7,4,6.5]);

		cube([5,3,4]);
		
		translate([0.3, -1.5, 1])
		cube([4.4,4,6]);
	}
}


// Holes used to hold the case.
module mount_holes()
{
	translate([0, 10, 0])
	cylinder(d=3.5, h=20, $fs = 1);

	translate([0, 15, 0])
	cylinder(d=3.5, h=20, $fs = 1);

	translate([0, 25, 0])
	cylinder(d=3.5, h=20, $fs = 1);

	translate([0, 20, 0])
	cylinder(d=3.5, h=20, $fs = 1);
}


// Holes used to hold the case.
module mount_holes_h()
{
	translate([-10, 0, 0])
	cylinder(d=3.5, h=20, $fs = 1);

	translate([-15, 0, 0])
	cylinder(d=3.5, h=20, $fs = 1);

	translate([-20, 0, 0])
	cylinder(d=3.5, h=20, $fs = 1);

	translate([-25, 0, 0])
	cylinder(d=3.5, h=20, $fs = 1);
}


// The casing.
module case()
{
	union()
	{
		translate([-50, -40, -9])
		difference()
		{
			cube([122, 150, 43]);

			translate([3, 3, -3])
			cube([116, 144, 43]);

			translate([50,40,25])
			cylinder(h = 19.6, d2 = 35.8, d = 18.4);

// START Case stand holes

			rotate(90, [1,0,0])
			{
				translate([61, 0, -10])
				mount_holes();

				translate([61, 0, -160])
				mount_holes();
			}

			rotate(90, [0,1,0]){

				translate([0, 75, -10])
				mount_holes_h();
			
				translate([0,75,110])
				mount_holes_h();
			}

// END Case stand holes
			
		}


// START Hall boxes

		difference()
		{
			union()
			{
				difference()
				{
					translate([-2, 0, -2])
					cylinder(d=69, h=34);

					translate([-2, 0, -3])
					cylinder(d=65, h=38);	
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

			translate([-18,-35,-5])
			cube([100,70,38]);	

			translate([-34,-35,-5])
			rotate(65, [0,0,1])
			cube([30,30,38]);

			translate([-60,25,-5])
			rotate(-65, [0,0,1])
			cube([30,30.5,38]);	

			translate([-29,-28,-5])
			rotate(-65, [0,0,1])
			cube([30,30,38]);

			translate([-12,18,-5])
			rotate(45, [0,0,1])
			cube([30,30,38]);
		}	
// END hall boxes		

// START Motor holder
		translate([-15,60,11])
		{
			difference()
			{
				cube([30,36.5,20]);
				rotate(90, [1,0,0])
				translate([15,7.5,-32.6])
				{
					cylinder(d=24.5, h=30.7);
					translate([-4,-14,29])
					cube([8,20,5]);
					translate([-15.5, -13, -4.5])
					cube([31, 20, 5]);
					translate([-12.25,-10,0])
					cube([24.5,10,30.7]);

					translate([-7.5,0,25])
					cylinder(d=2.5, h=10);
					translate([7.5,0,25])
					cylinder(d=2.5, h=10);
				}
				translate([-5, 14, 3])
				cube([40, 6, 2]);

			}

		}

// END Motor holder


// START nut holders

		translate([53,9,26.1])
		bolt_hold();
		
		translate([32,69,26.1])
		rotate(90, [0, 0, 1])
		bolt_hold();
	
		translate([53,69,26.1])
		bolt_hold();		

		translate([-25,90,26.1])
		rotate(90, [0, 0, 1])
		bolt_hold();		

		translate([-25,50,26.1])
		rotate(180, [0, 0, 1])
		bolt_hold();		

// END nut holders

	}
}