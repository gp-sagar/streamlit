import streamlit as st
import numpy as np
from PIL import Image
import pandas as pd
import mysql.connector
from query import get_meter_data, get_tariff, get_all_tariff_data, user_info, supply_1ph, supply_3ph, date_range_1ph, date_range_3ph, get_query_based, get_last_recharge_data, get_total_recharge_data, get_prepaid_status, get_commands_data, get_eb_dg_status_tariff_data,get_relay_status_load_data, get_replace_meter_data, get_churned_meter_data


st.set_page_config(
    page_title="Meter Data Dashboard",
    page_icon="img/favicon.png",
    layout="wide",
)


# Logo
image = Image.open('img/grampower.png')
st.image(image, caption='More Power to you')

# Heading
st.title("Meter Data Dashboard")

# Refresh Button
if st.button('Refresh'):
    st.experimental_rerun()

# Dashboard select
dashboard = st.selectbox(
    'Select Dashboard', (
        'Please Select Dashboard',
        'Meter Data Dashboard',
        'Replace Meter Details'
    )
)

if dashboard == 'Please Select Dashboard':
    warning = '<p style="font-family:sans-serif; color:red; font-size: 15px;">Please Select Dashboard</p>'
    st.markdown(warning, unsafe_allow_html=True)
elif dashboard == 'Meter Data Dashboard':
    query_based = st.text_input('Please Enter Meter Serial / Sc No / Meter Ip')
    if len(query_based) > 1:
        # Get Sc_no, supply_type, meter_address, grid_id
        sc_no = ''
        supply_type = ''
        meter_address = ''
        grid_id = ''
        rows = get_query_based(query_based)
        if len(rows) > 0:
            sc_no = rows['sc_no'][0]
            supply_type = rows['supply_type'][0]
            meter_address = rows['meter_address'][0]
            grid_id = rows['grid_id'][0]
            st.subheader('Meter Details')
            # Meter Survey Data
            meter_data = st.checkbox('Meter Survey Details')
            if meter_data:
                # Meter Data
                get_meter_data(query_based)
                # Tariff Data
                tariff_data = st.checkbox('Tariff Details')
                if tariff_data:
                    get_tariff(sc_no)
                    
                    # All tariff Data
                    all_tariff_data = st.checkbox('All Tariff Details')
                    if all_tariff_data:
                        get_all_tariff_data(sc_no)
            # Meter User Data
            st.subheader('User Details')
            user_data = st.checkbox('User Info')
            if user_data:
                user_info(sc_no)

            # Data View Using Date Range and Limit
            custom_data = st.checkbox('View custom Data')
            if custom_data:
                # Set Limit
                limit_range = st.checkbox('Set Limit')
                if limit_range:
                    limit = st.text_input('Enter for view data limit')
                    if limit:
                        # Single Phase
                        if supply_type == '1-Ph':
                            supply_1ph(meter_address, grid_id, limit)
                        elif supply_type == '3-Ph':
                            supply_3ph(meter_address, grid_id, limit)
                # Set Date Range
                date_range = st.checkbox('Set Date Range')
                if date_range:
                    start_date = st.date_input('Start Date')
                    end_date = st.date_input('End Date')
                    view_data = st.checkbox('View Data')
                    if view_data:
                        # Single Phase
                        if supply_type == '1-Ph':
                            date_range_1ph(grid_id, meter_address, start_date, end_date)
                        # Three Phase
                        if supply_type == '3-Ph':
                            date_range_3ph(grid_id, meter_address, start_date, end_date)
            # Recharge Details
            a = st.subheader('Recharge Details')
            # st.checkbox(st.subheader('Recharge'))
            recharge_details = st.checkbox('Recharge Amount')
            if recharge_details:
                # Last Recharge
                last_recharge = st.checkbox('Last recharge amount and time')
                if last_recharge:
                    get_last_recharge_data(sc_no)
                # Total Recharge
                total_recharge = st.checkbox('Total recharges amount till date')
                if total_recharge:
                    get_total_recharge_data(sc_no)
            # Status
            st.subheader('Status')
            # Prepaid Status
            status = st.checkbox('Prepaid Status')
            if status:
                get_prepaid_status(meter_address)
            # EB DG Status / EB DG Tarrif
            eb_dg_status_tariff = st.checkbox('EB DG Status and EB/ DG Tariff')
            if eb_dg_status_tariff:
                get_eb_dg_status_tariff_data(meter_address)
            # Relay Status load
            relay_status_load = st.checkbox('Relay status and Load')
            if relay_status_load:
                get_relay_status_load_data(supply_type, meter_address, grid_id)
            # Other Commands dta
            st.subheader('Other Data')
            command_data = st.checkbox('Other commands data')
            if command_data:
                get_commands_data(meter_address)

        else:
            st.warning('Please Enter a valid input')
elif dashboard == 'Replace Meter Details':
    st.subheader('Replace Meter Details')
    replace_or_churned = st.text_input('Please Enter Sc No')
    if len(replace_or_churned) >= 1:
        # Replaced Meter
        replaced_meter = st.checkbox('replaced Meter')
        if replaced_meter:
            get_replace_meter_data(replace_or_churned)
        # Churned Meter
        churned_meter = st.checkbox('Churned Meter')
        if churned_meter:
            get_churned_meter_data(replace_or_churned)