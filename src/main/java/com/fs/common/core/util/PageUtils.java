package com.fs.common.core.util;

import com.fs.common.core.exception.FsCommonException;
import com.fs.common.core.exception.ErrorType;
import com.fs.common.core.exception.ServiceStatusCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PageUtils {
    public static Page getPage(int num, int size) {
        return new Page(num, size);
    }

    public static Page getPage(int num, int size, int totalCount) {
        return new Page(num, size, totalCount);
    }

    public static <T> List<T> getPagedList(int num, int size, List<T> list) {
        Page page = getPage(num, size, list.size());

        return list.subList(page.getFirstIdx(), page.getLastIdx());
    }

    public static class Page {
        @Getter private final int num;
        @Getter private final int size;
        @Getter private int totalCount = -1;

        private Page(int num, int size) {
            this.num = num;
            this.size = size;

            if (getNum() <= 0) throw new FsCommonException(ErrorType.ERROR_SYSTEM, ServiceStatusCode.ERROR_READ, "Page number can't be 0 or negative number!");
            if (getSize() <= 0) throw new FsCommonException(ErrorType.ERROR_SYSTEM, ServiceStatusCode.ERROR_READ, "Page size can't be 0 or negative number!");
        }

        private Page(int num, int size, int totalCount) {
            this(num, size);
            this.totalCount = totalCount;

            if (getTotalCount() < 0) throw new FsCommonException(ErrorType.ERROR_SYSTEM, ServiceStatusCode.ERROR_READ, "Total amount of paged list can't be negative number!");
            if (getNum() > getLastPage()) {
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("lastPage", getLastPage());

                ResultInfoUtil.setAdditionalResultData(resultData);
                throw new FsCommonException(ErrorType.ERROR_SYSTEM, ServiceStatusCode.ERROR_READ, "Page number is bigger than last page!");
            }
        }

        public final int getFirstIdx() {
            return (getNum()-1)* getSize();
        }

        public final int getLastIdx() {
            return getFirstIdx()+ getSize()-1;
        }

        public final int getLastPage() throws UnsupportedOperationException {
            if (getTotalCount() > 0) {
                return (getTotalCount() - 1) / getSize() + 1;
            } else if (getTotalCount() == 0) {
                return 0; // empty list 등 0개일 경우엔 LastPage도 0으로 리턴
            } else {
                throw new UnsupportedOperationException("Please set 'totalCount' property if you want to use getLastPage()");
            }
        }
    }
}
