/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.xkms.exception;

import org.apache.cxf.xkms.model.xkms.ResultMajorEnum;
import org.apache.cxf.xkms.model.xkms.ResultMinorEnum;

public class XKMSRequestException extends XKMSException {

    private static final long serialVersionUID = 868729742068991783L;

    public XKMSRequestException() {
        super(ResultMajorEnum.HTTP_WWW_W_3_ORG_2002_03_XKMS_SENDER,
              ResultMinorEnum.HTTP_WWW_W_3_ORG_2002_03_XKMS_FAILURE);
    }

    public XKMSRequestException(String arg0) {
        super(ResultMajorEnum.HTTP_WWW_W_3_ORG_2002_03_XKMS_SENDER,
              ResultMinorEnum.HTTP_WWW_W_3_ORG_2002_03_XKMS_FAILURE, arg0);
    }

    public XKMSRequestException(String arg0, Throwable arg1) {
        super(ResultMajorEnum.HTTP_WWW_W_3_ORG_2002_03_XKMS_SENDER,
              ResultMinorEnum.HTTP_WWW_W_3_ORG_2002_03_XKMS_FAILURE, arg0, arg1);
    }

}
