/**
 * 
 */
package com.hesine.hichat.access.dao;

import java.util.List;
import java.util.Map;

import com.hesine.hichat.model.DoctorStatusInfo;

/**
 * @author pineapple
 *
 */
public interface DoctorDAO {

	public List<DoctorStatusInfo> getDoctorListWithParamMpa(Map<String, Object> paramMap);
	
}
