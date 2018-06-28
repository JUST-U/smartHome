/**
 * COPYRIGH (C) ZHEJIANG SCI-TECH UNIVERSITY. ALL RIGHTS RESERVED.
 * <p>
 * THE WORK (AS DEFINED BELOW) IS PROVIDED UNDER THE TERMS OF ZSTU LICENSE.
 * THE WORK IS PROTECTED BY COPYRIGHT AND/OR OTHER APPLICABLE LAW.
 * ANY USE OF THE WORK OTHER THAN AS AUTHORIZED UNDER THIS LICENSE OR COPYRIGHT LAW IS PROHIBITED.
 * <p>
 * BY EXERCISING ANY RIGHTS TO THE WORK PROVIDED HERE, YOU ACCEPT AND AGREE TO BE BOUND BY THE TERMS OF THIS LICENSE.
 * TO THE EXTENT THIS LICENSE MAY BE CONSIDERED TO BE A CONTRACT, THE LICENSOR GRANTS YOU
 * THE RIGHTS CONTAINED HERE IN CONSIDERATION OF YOUR ACCEPTANCE OF SUCH TERMS AND CONDITIONS.
 */
package cn.edu.zstu;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusProperty;
import org.alljoyn.bus.annotation.BusSignal;

/**
 * The IrTransponder interface describing how a IrTransponder looks on the AllJoyn bus.
 */
@BusInterface(name = IrTransponder.INTF_NAME, announced = "true")
public interface IrTransponder {

    String INTF_NAME = "cn.edu.zstu.IrTransponder";
    String SIGNAL_NAME = "jsonDataChanged";
    String PROPERTY_NAME = "jsonData";

    @BusProperty(annotation = BusProperty.ANNOTATE_EMIT_CHANGED_SIGNAL)
    String getjsonData() throws BusException;  // 注意形式 JsonData 为属性 ， getJsonData 为方法；

    @BusSignal(name = SIGNAL_NAME)
    void jsonDataChanged(String person) throws BusException;

}
