package com.fs.common.core.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.Serializable;

import static com.fs.common.core.exception.ServiceStatusCode.FAILED;
import static com.fs.common.core.exception.ServiceStatusCode.SUCCESS;


@Getter
@Setter
public class ResultInfo implements Serializable {

	private static final long serialVersionUID = 7021191539395886945L;

	@JsonDeserialize(using = ResultInfoDeserializer.class)
	private int code;

	// 에러/실패시 표시되는 상세 메세지, CommonException 포맷에 맞춰 시간/에러코드/상세내용이 표시됨
	private String message;
	// 성공시 표시되는 실제 리스폰스
	private Object data;



	public static class ResultInfoDeserializer extends JsonDeserializer<Integer> {
		@Override
		public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
			String raw = parser.getValueAsString();
			if (raw == null) return FAILED.getError();
			if (raw.matches("^\\d+$")) return Integer.parseInt(raw);

			if ("SUCCESS".equals(raw)) {
				return SUCCESS.getError();
			} else {
				return FAILED.getError();
			}
		}
	}
	/*public String toJson() {
		return JsonUtils.toJson(this);
	}*/
}
