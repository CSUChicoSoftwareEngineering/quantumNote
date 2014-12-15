import static org.junit.Assert.*;

import org.junit.Test;


public class FpointTest {

	@Test
	public void test1FpointFloatFloat() {
		float x = 3.14f;
		float y = .000667f;
		Fpoint pointy = new Fpoint(x,y);
		assertEquals(x, pointy.x,0);
		assertEquals(y, pointy.y,0);
	}

	@Test
	public void test2FpointFloatFloat() {
		float x = 0f;
		float y = -243058.2f;
		Fpoint pointy = new Fpoint(x,y);
		assertEquals(x, pointy.x,0);
		assertEquals(y, pointy.y,0);
	}

	@Test
	public void testFpointFpoint() {
		float x = -27f;
		float y = 167f;
		Fpoint pointy = new Fpoint(x,y);
		Fpoint pointier = new Fpoint(pointy);
		assertEquals(x, pointier.x,0);
		assertEquals(y, pointier.y,0);
	}

	@Test
	public void test1DistanceTo() {
		Fpoint point1 = new Fpoint(0f, 0f);
		Fpoint point2 = new Fpoint(4f, 3f);
		float dist = point1.distanceTo(point2);
		assertEquals(5, dist, 0);
	}

	@Test
	public void test2DistanceTo() {
		Fpoint point1 = new Fpoint(-4.63f, .0064f);
		Fpoint point2 = new Fpoint(35f, 249f);
		float dist = point1.distanceTo(point2);
		assertEquals(252.128, dist, .05);
	}

	@Test
	public void test1Cross2D() {
		Fpoint point1 = new Fpoint(5f, 10f);
		Fpoint point2 = new Fpoint(20f, 1f);
		float crossA = point1.cross2D(point2);
		float crossB = point2.cross2D(point1);
		assertEquals(crossA, -1*crossB, 0);
	}
	
	@Test
	public void test2Cross2D() {
		Fpoint point1 = new Fpoint(5f, 10f);
		Fpoint point2 = new Fpoint(20f, 1f);
		float cross = point1.cross2D(point2);
		assertEquals(-195, cross, .05);
	}
	
	@Test
	public void test3Cross2D() {
		Fpoint point1 = new Fpoint(.0003f, 1.0245f);
		Fpoint point2 = new Fpoint(204647.1f, .12341f);
		float cross = point1.cross2D(point2);
		assertEquals(-209661, cross, 1);
	}

	@Test
	public void test1Minus() {
		Fpoint point1 = new Fpoint(15f, 10f);
		Fpoint point2 = new Fpoint(25f, 1f);
		Fpoint point3 = point1.minus(point2);
		assertEquals(-10, point3.x, 0);
		assertEquals(9, point3.y, 0);
	}

	@Test
	public void test2Minus() {
		Fpoint point1 = new Fpoint(345f, -5645f);
		Fpoint point2 = new Fpoint(-23f, .034f);
		Fpoint point3 = point1.minus(point2);
		Fpoint point4 = point2.minus(point1);
		assertEquals(point4.x, -1*point3.x, 0);
		assertEquals(point4.y, -1*point3.y, 0);
	}
	
	@Test
	public void test1Magnitude() {
		Fpoint point = new Fpoint(235,793);
		float mag = point.magnitude();
		assertEquals(827.088, mag, .05);
	}
	
	@Test
	public void test2Magnitude() {
		Fpoint point = new Fpoint(-.00023f,.0000143f);
		float mag = point.magnitude();
		assertEquals(.000230444, mag, .05);
	}

	@Test
	public void test1DistanceToLine() {
		Fpoint point1 = new Fpoint(24, 16);
		Fpoint point2 = new Fpoint(2, 5);
		Fpoint point3 = new Fpoint(3, 7);
		float dist = point1.distanceToLine(point2, point3);
		assertEquals(14.758, dist, .05);
	}

	@Test
	public void test2DistanceToLine() {
		Fpoint point1 = new Fpoint(.032f, -3.425f);
		Fpoint point2 = new Fpoint(23f, 56.7f);
		Fpoint point3 = new Fpoint(-12f, -15.2f);
		float distA = point1.distanceToLine(point2, point3);
		float distB = point1.distanceToLine(point3, point2);
		assertEquals(distA, distB, .05);
	}

	@Test
	public void test3DistanceToLine() {
		Fpoint point1 = new Fpoint(.032f, -3.425f);
		Fpoint point2 = new Fpoint(23f, 56.7f);
		Fpoint point3 = new Fpoint(23f, 56.7f);
		float distA = point1.distanceToLine(point2, point3);
		assertEquals(Float.NaN, distA, .05);
	}

	@Test
	public void test4DistanceToLine() {
		Fpoint point1 = new Fpoint(0f, 0f);
		Fpoint point2 = new Fpoint(1f, 1f);
		Fpoint point3 = new Fpoint(2f, 2f);
		float distA = point1.distanceToLine(point2, point3);
		assertEquals(0, distA, .05);
	}

	@Test
	public void test5DistanceToLine() {
		Fpoint point1 = new Fpoint(100f, 50f);
		Fpoint point2 = new Fpoint(2f, 1f);
		Fpoint point3 = new Fpoint(14f, 7f);
		float distA = point1.distanceToLine(point2, point3);
		assertEquals(0, distA, .05);
	}

	@Test
	public void test1IsBetween() {
		Fpoint point1 = new Fpoint(24, 16);
		Fpoint point2 = new Fpoint(2, 5);
		Fpoint point3 = new Fpoint(3, 7);
		boolean state = point1.isBetween(point2, point3, 5);
		assertFalse(state);
	}

	@Test
	public void test2IsBetween() {
		Fpoint point1 = new Fpoint(.032f, -3.425f);
		Fpoint point2 = new Fpoint(23f, 56.7f);
		Fpoint point3 = new Fpoint(23f, 56.7f);
		boolean state = point1.isBetween(point2, point3, 5);
		assertFalse(state);
	}

	@Test
	public void test3IsBetween() {
		Fpoint point1 = new Fpoint(0f, 0f);
		Fpoint point2 = new Fpoint(1f, 1f);
		Fpoint point3 = new Fpoint(2f, 2f);
		boolean state = point1.isBetween(point2, point3, 5);
		assertFalse(state);
	}

	@Test
	public void test4IsBetween() {
		Fpoint point1 = new Fpoint(100f, 50f);
		Fpoint point2 = new Fpoint(2f, 1f);
		Fpoint point3 = new Fpoint(14f, 7f);
		boolean state = point1.isBetween(point2, point3, 5);
		assertFalse(state);
	}

	@Test
	public void test5IsBetween() {
		Fpoint point1 = new Fpoint(100f, 50f);
		Fpoint point2 = new Fpoint(2f, 1f);
		Fpoint point3 = new Fpoint(14f, 7f);
		boolean state = point3.isBetween(point1, point2, 5);
		assertTrue(state);
	}

	@Test
	public void test6IsBetween() {
		Fpoint point1 = new Fpoint(0f, 0f);
		Fpoint point2 = new Fpoint(1f, 1f);
		Fpoint point3 = new Fpoint(2f, 2f);
		boolean state = point2.isBetween(point1, point3, 5);
		assertTrue(state);
	}

}
