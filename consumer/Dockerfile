FROM python:3.11-alpine3.18

WORKDIR /code

COPY ./requirements.txt .
COPY ./consumer.py .

RUN pip3 install -r ./requirements.txt

CMD ["python3", "consumer.py"]
