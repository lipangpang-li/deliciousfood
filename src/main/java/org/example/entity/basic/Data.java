package org.example.entity.basic;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
public class Data<T> {
    private String status;
    private String message;
    private Pagination pagination;
    private int total;
    private Meta meta;
    private List<T> results;

    private T result;

    // 默认构造函数
    public Data() {
        this.status = "success";
        this.message = "Data retrieved successfully";
        this.meta = new Meta();
        this.meta.setRequestTimestamp(ZonedDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        this.meta.setVersion("1.0");
        this.results = List.of(); // 默认为空集合
    }

    // 带参数的构造函数
    public Data(List<T> results, int total, int currentPage, int totalPages, int pageSize) {
        this();
        this.results = results;
        this.total = total;
        this.pagination = new Pagination();
        this.pagination.setCurrentPage(currentPage);
        this.pagination.setTotalPages(totalPages);
        this.pagination.setPageSize(pageSize);
    }

    // Nested classes for Pagination and Meta
    public static class Pagination {
        private int currentPage;
        private int totalPages;
        private int pageSize;

        // Getters and Setters
        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }
    }

    public static class Meta {
        private String requestTimestamp;
        private String version;

        // Getters and Setters
        public String getRequestTimestamp() {
            return requestTimestamp;
        }

        public void setRequestTimestamp(String requestTimestamp) {
            this.requestTimestamp = requestTimestamp;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
