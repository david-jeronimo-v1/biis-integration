# biis-integration

Home MTA

## Install

Install Leiningen
On Windows:

* Download lein.bat: https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein.bat
* Add it to path

For IntelliJ:

* Install Cursive plugin

## Configure

Config: [config.clj](src/biis_integration/config/config.clj)

Input: [input.clj](src/biis_integration/config/input.clj)

* Specify a journey and the keys you want to log (optional)
* Set the payloads that you want to ingest into the journey. Different options for each one.

## Available journeys

### [mta-adjust](src/biis_integration/journey.clj#L76)
This journey will retrieve an existing quote and apply the specified adjustment to get a temporary quote.
The adjustment won't be saved in Applied
* policy-code
* output-folder
* update-cover-details-override (to override updateCoverDetails request) [spec](https://appliedsystems.stoplight.io/docs/applied-connect-api/33f5384174c15-adjustment-cover-details)
* temp-quote-start (to override getTemporaryQuote start date) [spec](https://appliedsystems.stoplight.io/docs/applied-connect-api/kji37kw3z9wai-adjustment-quote)

### [full-mta-journey](src/biis_integration/journey.clj#L50)
1. Create a quote in the wallet
1. bind it in Applied
1. apply and save adjustment
1. if the new amount is greater than zero, pay for it and accept the adjustment

Required fields (see [integration tests](test/biis_integration/full_home_mta_journey_test.clj):
* output-folder
* create-quote-override Override the [sample request](resources/createQuote.request.json) to create the quote
* update-cover-details-override (to override updateCoverDetails request) [spec](https://appliedsystems.stoplight.io/docs/applied-connect-api/33f5384174c15-adjustment-cover-details)
* temp-quote-start (to override getTemporaryQuote start date) [spec](https://appliedsystems.stoplight.io/docs/applied-connect-api/kji37kw3z9wai-adjustment-quote)

## Run

Run:

* lein.bat run

Run unit tests:
* lein.bat test

Output: Under 'location' folder. One folder created for each entry in [input](src/biis_integration/config/input.clj), as specified in the output-folder property

## License

Copyright © 2025 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
