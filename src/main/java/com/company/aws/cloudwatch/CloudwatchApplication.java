package com.company.aws.cloudwatch;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
import com.amazonaws.services.cloudwatch.model.Metric;
import com.amazonaws.services.logs.AWSLogs;
import com.amazonaws.services.logs.AWSLogsClient;
import com.amazonaws.services.logs.AWSLogsClientBuilder;
import com.amazonaws.services.logs.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;


@SpringBootApplication
public class CloudwatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudwatchApplication.class, args);

        BasicAWSCredentials basicAWSCredentials = getBasicAWSCredentials();
        //s3(basicAWSCredentials);
        //cloudwatchMetrics(basicAWSCredentials);

        AWSLogs awsLogs = AWSLogsClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials)).build();

        String logGroupName = "access_log";
        /*DescribeLogStreamsRequest describeLogStreamsRequest = new DescribeLogStreamsRequest(logGroupName);
        DescribeLogStreamsResult describeLogStreamsResult = awsLogs.describeLogStreams(describeLogStreamsRequest);
        describeLogStreamsResult.getLogStreams().stream().forEach(System.out::println);
*/

        GetLogEventsRequest request = new GetLogEventsRequest().withLogGroupName("access_log").withLogStreamName("i-0c57998613abc4318");
        GetLogEventsResult result = awsLogs.getLogEvents(request);

        String stringToFind = "2020:10:10:32";
        boolean found = false;

        for(OutputLogEvent event : result.getEvents()) {
            if(event.getMessage().contains(stringToFind)) {
                found = true;
                break;
            }
        }
        System.out.println(found);
    }

    private static BasicAWSCredentials getBasicAWSCredentials() {
        return new BasicAWSCredentials("", "");
    }

    public static void s3(BasicAWSCredentials basicAWSCredentials) {
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials)).build();
        List<Bucket> buckets = s3.listBuckets();

        buckets.stream().forEach(System.out::println);
    }

    public static void cloudwatchMetrics(BasicAWSCredentials basicAWSCredentials) {
        String metricName = "NetworkIn";
        String metricNamespace = "AWS/EC2";

        AmazonCloudWatch cw = AmazonCloudWatchClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials)).build();
        ListMetricsRequest request = new ListMetricsRequest()
                .withMetricName(metricName)
                .withNamespace(metricNamespace);

        boolean done = false;
        while (!done) {
            ListMetricsResult response = cw.listMetrics(request);
            for(Metric metric: response.getMetrics()) {
                System.out.println(metric);
            }
            request.setNextToken(response.getNextToken());
            if(response.getNextToken() == null) {
                done = true;
            }
        }
    }

}
