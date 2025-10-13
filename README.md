# How To

## AWS Setup

### AWS default credentials chain
The AWS SDK for Java (v2, which your project uses) automatically looks for credentials in this order:
1. 
2. Environment variables
```
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
```
You can set them on your system, in your Docker container, or in your CI/CD environment.

2. AWS credentials file
Usually located at:
```
~/.aws/credentials (Linux/Mac)
```

Example content:
```
[default]
aws_access_key_id = YOUR_ACCESS_KEY
aws_secret_access_key = YOUR_SECRET_KEY
```

### Configuration
**Option A: Using aws configure**
1.	Install the AWS CLI if not already installed.
2.	Run the command:

```bash
aws configure
```

3. Enter your AWS Access Key ID, Secret Access Key, default region, and output format.
Example:
```
AWS Access Key ID [None]: <YOUR_ACCESS_KEY>
AWS Secret Access Key [None]: <YOUR_SECRET_KEY>
Default region name [None]: eu-south-1
Default output format [None]: json
```

***Option B: Using Environment Variables***
Set the following environment variables before starting the application:
```
export AWS_ACCESS_KEY_ID=<YOUR_ACCESS_KEY>
export AWS_SECRET_ACCESS_KEY=<YOUR_SECRET_KEY>
export AWS_DEFAULT_REGION=eu-south-1
```

## App Usage
Start the Spring Boot app
```
./mvnw spring-boot:run`
```

Trigger the download via curl
```
curl -X POST "http://localhost:8080/download?prefix=backup/2024/"
```