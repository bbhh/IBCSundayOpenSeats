import React, { useState, useEffect } from 'react';
import { Jumbotron, FormGroup, Label, Input, Row, Col, Spinner, Alert } from 'reactstrap';
import RangeSlider from 'react-bootstrap-range-slider';
import axios from 'axios';
import queryString from 'query-string';

const API_BASE_URL = process.env.NODE_ENV === 'production' ? '' : 'http://localhost:7777';

enum PageState { LOADING, ERROR, READY }

const App = () => {
  const [name, setName] = useState('');
  const [attending, setAttending] = useState(false);
  const [sundayDate, setSundayDate] = useState('');
  const [partySize, setPartySize] = useState(1);
  const [expirationDate, setExpirationDate] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const [pageState, setPageState] = useState(PageState.LOADING);

  const { t: token } = queryString.parse(window.location.search);

  useEffect(() => {
    const fetch = async () => {
      console.log('Retrieving response...');
      try {
        const result = await axios(`${API_BASE_URL}/responses/${token}`);
        const { name, sundayDate, partySize, attending, expirationDate } = result.data;
        setName(name);
        setSundayDate(sundayDate);
        setPartySize(partySize);
        setAttending(attending);
        setExpirationDate(expirationDate);

        setPageState(PageState.READY)
      } catch (error) {
        setPageState(PageState.ERROR)
        if (error.response) {
          setErrorMessage(error.response.data);
        }
      }
    };

    fetch();
  }, [token]);

  useEffect(() => {
    const updateResponse = async () => {
      console.log(`Updating response with [partySize=${partySize} attending=${attending}]...`);
      try {
        await axios.put(`${API_BASE_URL}/responses/${token}`, {
          partySize,
          attending,
        });
      } catch (error) {
        setPageState(PageState.ERROR)
        if (error.response) {
          setErrorMessage(error.response.data);
        }
      }
    };

    if (pageState === PageState.READY) updateResponse();
  }, [token, pageState, partySize, attending]);
  

  function handlePartySizeChange(partySize: number) {
    setPartySize(partySize);
  }

  let content = null;
  if (pageState === PageState.LOADING) {
    content = <Spinner />;
  }
  else if (pageState === PageState.READY) {
    const partySizeSlider = attending && (
      <Row>
        <Col sm={4}>
          <p className="lead">How many people will be attending in your party, including yourself?</p>
          <RangeSlider
            value={partySize}
            onChange={(e: React.ChangeEvent<any>) => handlePartySizeChange(e.target.value)}
            min={1}
            max={10}
            tooltip="on"
          />
        </Col>
      </Row>
    );

    content = (
      <div>
        <p className="lead">Hi <strong>{name}</strong>, will you be attending Immanuel Bible Church for outdoor worship service this coming <strong>Sunday, {sundayDate} at 9am</strong>?</p>
      
        <FormGroup tag="fieldset" style={{ fontSize: '150%' }}>
          <FormGroup check inline>
            <Label check className="text-success">
              <Input type="radio" name="radioAttending" checked={attending} onChange={e => setAttending(true)} />{' '}
              Yes
            </Label>
          </FormGroup>
          <FormGroup check inline className="ml-4">
            <Label check className="text-danger">
              <Input type="radio" name="radioAttending" checked={!attending} onChange={e => setAttending(false)} />{' '}
              No
            </Label>
          </FormGroup>
        </FormGroup>

        {partySizeSlider}

        <Alert color="success" className="mt-4">Your response is automatically saved, so there's no need to do anything else. Thanks!</Alert>

        {pageState === PageState.READY && (
          <p className="mt-5 text-muted">If you want make any changes to your response later, you can return to this page at any time before the deadline of <strong>{expirationDate}</strong>.</p>
        )}
      </div>
    );
  }
  else {
    content = <Alert color="danger">{errorMessage || 'Oops! Something went wrong.'}</Alert>;
  }



  return (
    <div>
      <Jumbotron>
        <h1 className="display-4 mb-4">IBC Sunday Seats</h1>
        
        {content}
      </Jumbotron>
    </div>
  );
};

export default App;
