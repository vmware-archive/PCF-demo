package com.pivotal.example.xd;

public class TimeItem {
    private Long timestamp;
      private String timeInString;

        public TimeItem(Long timestamp, String timeInString) {
              this.timestamp = timestamp;
                  this.timeInString = timeInString;
                    }

          public TimeItem() {
              }

            public Long getTimestamp() {
                  return timestamp;
                    }

              public void setTimestamp(Long timestamp) {
                    this.timestamp = timestamp;
                      }

                public String getTimeInString() {
                      return timeInString;
                        }

                  public void setTimeInString(String timeInString) {
                        this.timeInString = timeInString;
                          }
}
