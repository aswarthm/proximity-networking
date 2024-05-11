import vertexai
from vertexai.preview.generative_models import GenerativeModel
from google.oauth2 import service_account



credentials = service_account.Credentials.from_service_account_file('./spendwise.json')
vertexai.init( project="spendwise-405814", credentials=credentials)

config = {
    "max_output_tokens": 2048,
    "temperature": 0.5,
    "top_p": 1
}


def getGeminiResponse(prompt : str) -> str:
    model = GenerativeModel("gemini-pro")   # Start a new chat for every prompt or else it gets bored of saying similar things and starts inventing bullshit
    chat = model.start_chat()
    response = chat.send_message(prompt, generation_config=config).candidates[0].content.parts[0].text
    return response


def createPrompt(client_data, dev_data):


    return "HELLO"




if __name__ == "__main__":
    prompt = createPrompt(1,2)

    print(getGeminiResponse(prompt))