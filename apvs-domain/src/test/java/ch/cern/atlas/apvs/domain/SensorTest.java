package ch.cern.atlas.apvs.domain;

import java.util.List;

import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;

public class SensorTest extends AbstractDomainTest {

	@SuppressWarnings({ "rawtypes" })
	@Test
	public void testBasicUsage() {
		Device d1 = new Device("PTU_88", InetAddress.getByName("localhost"), "ptu 88", null, "ptu_88.cern.ch");
		Device d2 = new Device("PTU_99", InetAddress.getByName("localhost"), "ptu 99", null, "ptu_99.cern.ch");
		
		Sensor s1 = new Sensor(d1, "Temperature", true);
		Sensor s2 = new Sensor(d2, "Humidity", false);
		
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.save(d1);
		session.save(d2);
		session.save(s1);
		session.save(s2);
		session.getTransaction().commit();
		session.close();

		// now lets pull events from the database and list them
		session = sessionFactory.openSession();
		session.beginTransaction();
		List result = session.createCriteria(Sensor.class).list();
		Assert.assertEquals(2, result.size());
//		System.err.println(result.get(0));
		Assert.assertEquals(s1, result.get(0));
//		System.err.println(result.get(1));
		Assert.assertEquals(s2, result.get(1));
		session.getTransaction().commit();
		session.close();
	}

}
