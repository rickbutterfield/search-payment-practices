# payment-practices-reporting
[![CircleCI](https://circleci.com/gh/UKGovernmentBEIS/search-payment-practices.svg?style=svg)](https://circleci.com/gh/UKGovernmentBEIS/search-payment-practices)

## Configuration

### Companies House API
You'll need to obtain an API key for making calls to the Companies House API. Go to the 
Companies House [developer hub](https://developer.companieshouse.gov.uk/api/docs/),
register an account and create a new application. One of the pieces of information
provided is an API key. You can provide this key to the application by setting the
`COMPANIES_HOUSE_API_KEY` environment variable prior to starting `sbt`.

Similarly, for production, inject the api key value into the environment with that
env variable.


### Google Analytics

Set an environment variable `GA_CODE` to the tracking code for your google analytics
account in order to enable tracking.
 
