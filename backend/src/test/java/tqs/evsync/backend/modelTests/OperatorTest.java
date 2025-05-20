package tqs.evsync.backend.modelTests;

import org.junit.jupiter.api.Test;
import tqs.evsync.backend.model.Operator;
import tqs.evsync.backend.model.enums.OperatorType;
import tqs.evsync.backend.model.ChargingStation;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class OperatorTest {

    @Test
    void testOperatorTypeGetterSetter() {
        Operator operator = new Operator();
        operator.setOperatorType(OperatorType.OPERATOR);
        assertEquals(OperatorType.OPERATOR, operator.getOperatorType());
    }

    @Test
    void testChargingStationList() {
        Operator operator = new Operator();
        operator.setChargingStation(new ArrayList<>());
        ChargingStation station = new ChargingStation();
        operator.addChargingStation(station);
        assertEquals(1, operator.getChargingStation().size());
        assertEquals(operator, station.getOperator());
    }
}
