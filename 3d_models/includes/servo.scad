/*
	Servo with the join. This servo is used to move the gear.
	NOT TO BE PRINTED!
	AUTHOR: Albert Viaplana Ventura
	Network Embedded Systems
	TU Wien 2015
*/

// This part is used to join the servo with the gear.
module join()
{
	difference()
	{
		cylinder(d=21.1, h=2.2, $fs = 0.5);

		translate([8.05, 0, -.5])
		cylinder(d=1.6, h=3, $fs = 0.5);

		translate([-8.05, 0, -.5])
		cylinder(d=1.6, h=3, $fs = 0.5);

		translate([0, -8.05, -.5])
		cylinder(d=1.6, h=3, $fs = 0.5);

		translate([0, 8.05, -.5])
		cylinder(d=1.6, h=3, $fs = 0.5);
	}
}


// The part used to fix the servo
module fix_servo()
{
	difference()
	{
		cube([8.1, 2.4, 19.6]);

		translate([3.55, 2.7, 4.45])
		rotate(90, [1, 0, 0])
		cylinder(d=4.5, h=3, $fs = 0.5);

		translate([3.55, 2.7, 15.15])
		rotate(90, [1, 0, 0])
		cylinder(d=4.5, h=3, $fs = 0.5);
	}
}


// NOT USED! 
module wing()
{
	B = 9.3;
	h = 15.3;
	b = 5.5;
	difference(){
		union() 
		{
			linear_extrude(height = 2.2)
			polygon( points = [
				[-B/2, -h/2],
				[-b/2, h/2],
				[b/2, h/2],
				[B/2, -h/2]
			], h = 2.2);
			translate([0, 7.185,0])
			cylinder(d=5.5, h=2.2, $fs = 0.5);
		}
		
		translate([0, 6.8, -.5])
		cylinder(d=2.2, h=3, $fs = 0.5);

		translate([0, 3.4, -.5])
		cylinder(d=2.2, h=3, $fs = 0.5);

		translate([0, 0, -.5])
		cylinder(d=2.2, h=3, $fs = 0.5);

		translate([0, -3.3, -.5])
		cylinder(d=2.2, h=3, $fs = 0.5);
	}
}


// NOT USED! Just a different type of join between the servo and gear.
module join_wings()
{
	union(){
		translate([-4.65, -16.95, 0])
		cube([9.3,9.3,2.2]);

		wing();

		translate([-12.3, -12.3, 0])
		rotate(90, [0, 0, 1])
		wing();

		translate([0, -24.6, 0])
		rotate(180, [0, 0, 1])
		wing();

		translate([12.3,-12.3,0])
		rotate(-90, [0, 0, 1])
		wing();
	}
}


// Model of the servo.
module servo()
{
	cube([40.2, 38, 19.9]);
	translate([48.3,10,.2])
	rotate(180, [0, 0, 1])
	fix_servo();

	translate([-8.1,7.6,.2])
	fix_servo();
	
	translate([10.5,0,10.5])
	rotate(90, [1, 0, 0])
	cylinder(d=6, h=7.2);

	translate([10.5, -7.2, 10])
	rotate(90, [1, 0, 0])
	join();
}
