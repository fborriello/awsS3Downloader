# AWS S3 Downloader - How To Guide

## 1. AWS Setup

### 1.1. AWS CLI Setup
To run the project it's needed to have the AWS CLI installed. Follow [this guide](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) to set it up.

### 1.2. Configuring AWS Credentials

The S3 Downloader application needs AWS credentials to access S3. **Do not store your AWS keys in code or configuration files**. Use one of the methods below.

#### 1.2.1. Using an IAM User (Long-term Credentials)

1. Sign in to the [AWS Management Console](https://aws.amazon.com/console/) with an account that has IAM permissions.
2. Go to **IAM → Users**.
3. Select an existing user or create a new one.
4. Ensure the user has **S3 access permissions** (e.g., attach `AmazonS3FullAccess` or a custom policy for your bucket).
5. Open the **Security credentials** tab.
6. Under **Access keys**, click **Create access key**.
7. Copy the **Access Key ID** and **Secret Access Key**.
   > **Important:** The secret key is shown only once. Save it securely.
8. Set them as environment variables before running the app:

```bash
export AWS_ACCESS_KEY_ID=<YOUR_ACCESS_KEY>
export AWS_SECRET_ACCESS_KEY=<YOUR_SECRET_KEY>
export AWS_DEFAULT_REGION=eu-south-1
```

#### 1.2.2. AWS Default Credentials Chain

The AWS SDK for Java (v2, which your project uses) automatically looks for credentials in this order:

1. **Environment variables**
   ```bash
   export AWS_ACCESS_KEY_ID=your-access-key
   export AWS_SECRET_ACCESS_KEY=your-secret-key
   export AWS_DEFAULT_REGION=eu-south-1
   ```
   You can set them on your system, in your Docker container, or in your CI/CD environment.

2. **AWS credentials file**

   Usually located at:
   - `~/.aws/credentials` (Linux/Mac)

   Example content:
   ```ini
   [default]
   aws_access_key_id = YOUR_ACCESS_KEY
   aws_secret_access_key = YOUR_SECRET_KEY
   ```

#### 1.2.3. Configuration Methods

- **Option A: Using aws configure**
  1. Install the AWS CLI if not already installed.
  2. Run the command:
     ```bash
     aws configure
     ```
  3. Enter your AWS Access Key ID, Secret Access Key, default region, and output format. Example:
     ```
     AWS Access Key ID [None]: <YOUR_ACCESS_KEY>
     AWS Secret Access Key [None]: <YOUR_SECRET_KEY>
     Default region name [None]: eu-south-1
     Default output format [None]: json
     ```

- **Option B: Using Environment Variables**
  Set the following environment variables before starting the application:
  ```bash
  export AWS_ACCESS_KEY_ID=<YOUR_ACCESS_KEY>
  export AWS_SECRET_ACCESS_KEY=<YOUR_SECRET_KEY>
  export AWS_DEFAULT_REGION=eu-south-1
  ```

---

## 2. App Usage

### 2.1. Start the Spring Boot App with a Rest API

```bash
./mvnw spring-boot:run
```

#### 2.1.2. Start the Spring Boot App with a specific AWS Profile
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--aws.profile=backup-user"
```

#### 2.1.3. Trigger the Download via curl

```bash
curl -X POST "http://localhost:8080/download?prefix=backup/2024/"
```

### 2.1.4 Trigger the Download via Postman project
If you have Postman installed, you can use the Postman Collection available in the project at: [test/postman](test/postman) to invoke the endpoint

### 2.2 Start the application from command line
```bash
java -jar aws-s3-downloader-1.0-SNAPSHOT.jar \
--aws.bucket=my-archive-bucket \
--aws.region=eu-south-1 \
--aws.profile=backup-user \
--prefix=archives/2025/ \
--downloadDir=./download
```